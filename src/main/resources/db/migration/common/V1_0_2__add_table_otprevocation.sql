create table otpRevocation
(
    jti varchar not null primary key,
    user_ext_id varchar not null,
    idpsource varchar not null,
    created_at timestamp not null
)