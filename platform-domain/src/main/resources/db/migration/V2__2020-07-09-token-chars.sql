ALTER TABLE account
    ALTER change_account_token TYPE VARCHAR(1024),
    ALTER fcm_token TYPE VARCHAR(1024);

ALTER TABLE call
    ALTER receiver_token TYPE VARCHAR(1024),
    ALTER sender_token TYPE VARCHAR(1024);

ALTER TABLE message
    ALTER text TYPE VARCHAR(2048);

ALTER TABLE multimedia
    ALTER remote_url TYPE VARCHAR(1024);
