package com.calendar.users.infrastructure.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
//@Profile({"dev", "test"})
public class DatabaseSeeder implements CommandLineRunner {

    private final static int USER_COUNT = 1_000_000;
    private final R2dbcEntityTemplate template;

    public DatabaseSeeder(R2dbcEntityTemplate template) {
        this.template = template;
    }

    @Override
    public void run(String... args) {
        System.out.println("Démarrage du seeding");
        seedUsers()
            .then(seedUserFriends())
            .doOnSuccess(v -> System.out.println("Seeding terminé"))
            .doOnError(e -> {
                System.err.println("Erreur lors du seeding: " + e.getMessage());
                e.printStackTrace();
            })
            .block();
    }

    /**
     * Insère jusqu'à USER_COUNT utilisateurs dans app_user en batchs multi-lignes.
     * Évite la réinsertion si la table contient déjà au moins USER_COUNT lignes.
     */
    private Mono<Void> seedUsers() {
        final int batchSize = 5_000;

        return template.getDatabaseClient()
            .sql("SELECT COUNT(*) FROM app_user")
            .map((row, md) -> row.get(0, Long.class))
            .first()
            .defaultIfEmpty(0L)
            .flatMap(count -> {
                if (count >= USER_COUNT) {
                    System.out.println("app_user déjà peuplée (" + count + " lignes) - skip");
                    return Mono.empty();
                }
                long toInsert = USER_COUNT - count;
                System.out.println("Insertion de " + toInsert + " utilisateurs dans app_user");
                int batches = (int) ((toInsert + batchSize - 1) / batchSize);

                return Flux.range(0, batches)
                    .concatMap(batchIndex -> {
                        int start = batchIndex * batchSize;
                        int size = (int) Math.min(batchSize, toInsert - start);

                        StringBuilder sql = new StringBuilder("INSERT INTO app_user (keycloak_id, profile_pic_url, joined_date) VALUES ");
                        for (int i = 0; i < size; i++) {
                            if (i > 0) sql.append(", ");
                            sql.append("(:k").append(i).append(", :p").append(i).append(", :j").append(i).append(")");
                        }

                        var spec = template.getDatabaseClient().sql(sql.toString());
                        for (int i = 0; i < size; i++) {
                            String keycloak = UUID.randomUUID().toString();
                            String profile = "https://example-bucket/" + keycloak;
                            LocalDateTime joined = LocalDateTime.now()
                                .minusDays(ThreadLocalRandom.current().nextInt(0, 3650));

                            spec = spec
                                .bind("k" + i, keycloak)
                                .bind("p" + i, profile)
                                .bind("j" + i, joined);
                        }

                        return spec.fetch().rowsUpdated()
                            .doOnNext(n -> System.out.println("app_user batch " + (batchIndex + 1) + "/" + batches + " -> " + n + " lignes"));
                    })
                    .then();
            });
    }

    /**
     * Insère jusqu'à USER_COUNT relations dans user_friends (user_id -> friend_id) en batchs multi-lignes.
     * Génère des paires simples (i -> i+1) dans l'intervalle [MIN(id), MAX(id)], en évitant la réinsertion si déjà atteint.
     */
    private Mono<Void> seedUserFriends() {
        final int batchSize = 5_000;

        var client = template.getDatabaseClient();

        Mono<Long> existing = client.sql("SELECT COUNT(*) FROM user_friends")
            .map((row, md) -> row.get(0, Long.class))
            .first()
            .defaultIfEmpty(0L);

        Mono<Long> minId = client.sql("SELECT MIN(id) FROM app_user")
            .map((row, md) -> row.get(0, Long.class))
            .first();

        Mono<Long> maxId = client.sql("SELECT MAX(id) FROM app_user")
            .map((row, md) -> row.get(0, Long.class))
            .first();

        Mono<Long> userCount = client.sql("SELECT COUNT(*) FROM app_user")
            .map((row, md) -> row.get(0, Long.class))
            .first()
            .defaultIfEmpty(0L);

        return Mono.zip(existing, minId, maxId, userCount)
            .flatMap(t -> {
                long existingCount = t.getT1();
                Long min = t.getT2();
                Long max = t.getT3();
                long users = t.getT4();

                if (users == 0 || min == null || max == null) {
                    System.out.println("user_friends: aucun utilisateur disponible - skip");
                    return Mono.empty();
                }
                if (existingCount >= USER_COUNT) {
                    System.out.println("user_friends déjà peuplée (" + existingCount + " lignes) - skip");
                    return Mono.empty();
                }

                long toInsert = USER_COUNT - existingCount;
                long range = (max - min + 1);
                int batches = (int) ((toInsert + batchSize - 1) / batchSize);

                System.out.println("Insertion de " + toInsert + " relations dans user_friends");

                return Flux.range(0, batches)
                    .concatMap(batchIndex -> {
                        int start = batchIndex * batchSize;
                        int size = (int) Math.min(batchSize, toInsert - start);

                        StringBuilder sql = new StringBuilder("INSERT INTO user_friends (user_id, friend_id) VALUES ");
                        for (int i = 0; i < size; i++) {
                            if (i > 0) sql.append(", ");
                            sql.append("(:u").append(i).append(", :f").append(i).append(")");
                        }

                        var spec = client.sql(sql.toString());
                        for (int i = 0; i < size; i++) {
                            long userId = min + ((start + i) % range);
                            long friendId = (userId == max) ? min : (userId + 1);
                            if (friendId == userId) {
                                friendId = (userId == min) ? max : (userId - 1);
                            }
                            spec = spec.bind("u" + i, userId).bind("f" + i, friendId);
                        }

                        return spec.fetch().rowsUpdated()
                            .doOnNext(n -> System.out.println("user_friends batch " + (batchIndex + 1) + "/" + batches + " -> " + n + " lignes"));
                    })
                    .then();
            });
    }

}
