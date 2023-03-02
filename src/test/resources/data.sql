insert into posts(title, description, content) values ('PostTitle-1','PostDescription','PostContent');
insert into posts(title, description, content) values ('PostTitle-2','PostDescription','PostContent');
insert into posts(title, description, content) values ('PostTitle-3','PostDescription','PostContent');
insert into posts(title, description, content) values ('PostTitle-4','PostDescription','PostContent');
insert into posts(title, description, content) values ('PostTitle-5','PostDescription','PostContent');

insert into comments(name, email, body, post_id) values ('CommentName-1','email-1@email.com','CommentBody-1',1);
insert into comments(name, email, body, post_id) values ('CommentName-2','email-2@email.com','CommentBody-2',1);
insert into comments(name, email, body, post_id) values ('CommentName-3','email-3@email.com','CommentBody-3',2);
insert into comments(name, email, body, post_id) values ('CommentName-4','email-4@email.com','CommentBody-4',2);

insert into roles(name) values ('ROLE_ADMIN'), ('ROLE_USER');

insert into users(name, username, email, password) values ('user-1', 'user-1@email.com', 'user-1@email.com', '$2a$10$Rni2NWvPgpw7n5ybC39uROxnoQTnIiBJgamO30ZWOD/yKdq7AhKIi');
insert into users(name, username, email, password) values ('user-2', 'user-2@email.com', 'user-2@email.com', '$2a$10$h8zFDkYGncsy8rkaLs614uQ9quwXtxNLKOtLcMmRiHAL6g7bxmCtG');

insert into user_roles(user_id, role_id) values (1, 1);
insert into user_roles(user_id, role_id) values (2, 2);