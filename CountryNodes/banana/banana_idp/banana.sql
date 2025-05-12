
CREATE TABLE `users`(
                        `uuid` varchar(36) NOT NULL,
                        `username` varchar(32) NOT NULL,
                        `password` varchar(256) NOT NULL,
                        `first_name` varchar(50) NOT NULL,
                        `last_name` varchar(50) NOT NULL,
                        `email` varchar(100) NOT NULL,
                        `dob` date NOT NULL,
                        `pin` varchar(20) NOT NULL,
                        `address` varchar(100) NOT NULL,
                        `gender` varchar(20) NOT NULL,
                        `age` int(11) GENERATED ALWAYS AS (timestampdiff(YEAR,`dob`,curdate())) VIRTUAL,
                        PRIMARY KEY (`uuid`),
                        UNIQUE KEY `uuid_unique` (`uuid`),
                        UNIQUE KEY `uuid_pin` (`uuid`,`pin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

LOCK TABLES `users` WRITE;
INSERT INTO `users`
(`uuid`, `username`, `password`, `first_name`, `last_name`, `email`, `dob`, `pin`, `address`, `gender`)
VALUES
    ('209c1864-e251-4ed8-887b-15ce24eb1bf1','Almo','$2b$10$mZx9Mm/.1wb5cyYRsGW.vOY6vybgG5Oawx1jr1UsP14dzVvrj6Vxq','Andrei','Iordache','andreialexandruiordache@gmail.com','1990-01-01','12345','Str. Principala nr 1','Male'),
    ('22db620a-0dc3-11ee-a70b-ca8b19b5aaf1','bog1200','$2b$10$SwgSDyVNsmv0W8/nOr3WR.H0pXHsrmMZgP8BdfNpSyN3s8w2uxz.q','Bogdan','Stefanescu','stefanescubogdan40@gmail.com','2002-09-13','12346','Str. Principala nr 1','Male'),
    ('86332b75-0e59-4590-afaa-0581323d37b4','Mario1','$2b$10$RX4npxrN9aQc3GYsTdmvkeZeUZKorcWddOK/KidetgMvZei.Mxlz2','Mario','Rodina','rodalexandru@gmail.com','1990-01-01','12347','Str. Principala nr 1','Male'),
    ('945b2dba-4d22-4de8-9275-000000000000','demo','$2b$10$sz9/piNL0KWB69AzTLs1UeQdcUji/3vH7JDPll4QxX8uMTKhQuTUK','Demo','User','auth@demo.romail.app','1990-01-01','12348','Str. Principala nr 1','Non-Binary'),
    ('aad4721a-b1ba-414f-a236-09c9a1ff3e15','teodorafils','$2b$10$b4I139MKOim2HglTLYa0NuFgF3HjRnpOaWRIjzKOMevh7zMC930pi','Teodora','Olteanu','teodora@romail.app','1990-01-01','12340','Str. Principala nr 1','Female');
UNLOCK TABLES;

CREATE TABLE `audit_log_actions`(
                                    `id` int(11) NOT NULL AUTO_INCREMENT,
                                    `action` varchar(50) NOT NULL,
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `action` (`action`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

LOCK TABLES `audit_log_actions` WRITE;
INSERT INTO `audit_log_actions` VALUES
                                    (1,'ACCOUNT_CREATED'),
                                    (4,'ACCOUNT_EMAIL_CHANGED'),
                                    (5,'ACCOUNT_NAME_CHANGED'),
                                    (9,'ACCOUNT_PASSKEY_CREATED'),
                                    (8,'ACCOUNT_PASSKEY_SIGNIN'),
                                    (3,'ACCOUNT_PASS_CHANGED'),
                                    (2,'ACCOUNT_SIGNIN'),
                                    (6,'ACCOUNT_SIGNIN_FAIL'),
                                    (7,'ACCOUNT_USERNAME_CHANGED');
UNLOCK TABLES;

CREATE TABLE `clients`(
                          `name` varchar(25) NOT NULL,
                          `client_id` varchar(50) NOT NULL,
                          `client_secret` varchar(50) NOT NULL,
                          `client_url` varchar(255) NOT NULL,
                          PRIMARY KEY (`client_id`),
                          UNIQUE KEY `name` (`name`),
                          UNIQUE KEY `client_id` (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

LOCK TABLES `clients` WRITE;
INSERT INTO `clients` VALUES
                          ('Banana Country Node','idp.banana','banana.secret','http://10.50.0.102:8080/api/identity/callback'),
                          ('OAuth Debugger','test.oauth','oauthsecret','https://oauthdebugger.com/debug');
UNLOCK TABLES;

CREATE TABLE `user_passkeys`(
                                `id` int(11) NOT NULL AUTO_INCREMENT,
                                `user` varchar(36) NOT NULL,
                                `token` varchar(255) NOT NULL,
                                `creation_time` timestamp NOT NULL DEFAULT current_timestamp(),
                                PRIMARY KEY (`id`),
                                KEY `user_passkeys_user` (`user`),
                                CONSTRAINT `user_passkeys_user` FOREIGN KEY (`user`) REFERENCES `users` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

LOCK TABLES `user_passkeys` WRITE;
UNLOCK TABLES;

CREATE TABLE `audit_log`(
                            `id` int(11) NOT NULL AUTO_INCREMENT,
                            `uuid` varchar(36) DEFAULT NULL,
                            `ip` varchar(128) NOT NULL,
                            `action` int(11) NOT NULL,
                            `data` varchar(100) DEFAULT NULL,
                            `timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
                            PRIMARY KEY (`id`),
                            KEY `audit_log_uuid` (`uuid`),
                            KEY `action` (`action`),
                            CONSTRAINT `audit_log_ibfk_1` FOREIGN KEY (`action`) REFERENCES `audit_log_actions` (`id`),
                            CONSTRAINT `audit_log_uuid` FOREIGN KEY (`uuid`) REFERENCES `users` (`uuid`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
