DELETE FROM gebruiker;

INSERT INTO gebruiker (id, gebruikersnaam, wachtwoord, rol) VALUES
(1, 'admin', '$2a$10$ed47ET2dBc3KzEmsM07W4.OTFFK/emSA0GEWH7GfoFXSOp6ym4SfS', 'ADMIN'),
(2, 'moderator', '$2a$10$XJnkl81I7zY9zBQx4Zt7ZuZQ2f2IdFukvbyOCJEX9ZkAgDF5jYJSG', 'MODERATOR');
