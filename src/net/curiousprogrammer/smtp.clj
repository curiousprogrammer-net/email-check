(ns net.curiousprogrammer.smtp
  "Primitive smtp client.
  For now, just enough functionality to be able to verify email recipients.

  TODO: this is work in progress and doesn't work apart from JDK 8.
  Alternative solution based on commons-net package might be implemented later."
  (:require [net.curiousprogrammer.dns :as dns]))

;;; Overview of SMTP transaction: https://www.linuxjournal.com/content/sending-email-netcat
;;; `check-recipient!` is an equivalent of this:
;;; -------------------------------------------
;;; nc -c mail2.mailinator.com 25
;;; HELO mailtrap.io
;;; MAIL FROM:<me@example.com>
;;; RCPT TO:<testikabcd@mailinator.com>
;;; QUIT
;;; ------------------------------------


;;; check-recipient implementation using internal JDK SmtpClient
(defn check-recipient!
  "Checks whether `mail-to` is a valid email address, presumably an existing recipient,
  at given `mail-host` email server (presumably found via MX record for `mail-to` email domain).

  Uses `mail-from` as 'MAIL FROM', otherwise it couldn't issue the 'RCPT TO' command.

  Returns a map with `:valid?` true or false depending on the result of the check.
  If `:valid?` is `false`, then also returns `:error` key with detailed error message.

  See https://mailtrap.io/blog/550-5-1-1-rejected-fix/ for more details about 550 smpt errors."
  [mail-host mail-from mail-to]
  ;; TODO: SmtpClient is an internal class not available in later JDKs
  (let [smtp-client (sun.net.smtp.SmtpClient. mail-host)]
    (try
      (.from smtp-client mail-from)
      (.to smtp-client mail-to)
      {:valid? true}
      (catch Exception e
        ;; Exception message should contain error details, e.g. "550-5.1.1 The email account that you tried to reach does not exist."
        {:valid? false :error (.getMessage e)})
      (finally (.closeServer smtp-client)))))

;; TODO: alternative check-recipient using commons-net:
;; https://commons.apache.org/proper/commons-net/javadocs/api-3.6/org/apache/commons/net/smtp/SMTPClient.html
#_(defn check-recipient!
  "Checks whether `mail-to` is a valid email address, presumably an existing recipient,
  at given `mail-host` email server (presumably found via MX record for `mail-to` email domain).

  Uses `mail-from` as 'MAIL FROM', otherwise it couldn't issue the 'RCPT TO' command.

  Returns a map with `:valid?` true or false depending on the result of the check.
  If `:valid?` is `false`, then also returns `:error` key with detailed error message.

  See https://mailtrap.io/blog/550-5-1-1-rejected-fix/ for more details about 550 smpt errors."
  [mail-host mail-from mail-to]
  (let [smtp-client (org.apache.commons.net.smtp.SMTPClient.)])
  )

(comment
  (check-recipient! (:target (net.curiousprogrammer.dns/valid-mx-record "mailinator.com"))
                    "me@example.com"
                    ;; https://www.mailinator.com/v4/public/inboxes.jsp?to=testikabcd
                    "testikabcd@mailinator.com")
  ;; => [true nil]

  (check-recipient! (:target (net.curiousprogrammer.dns/valid-mx-record "codescene.com"))
                    "me@example.com"
                    ;; https://www.mailinator.com/v4/public/inboxes.jsp?to=testikabcd
                    "sales@codescene.com")
  ;; => [true nil]
  (check-recipient! (:target (net.curiousprogrammer.dns/valid-mx-record "codescene.com"))
                    "me@example.com"
                    ;; https://www.mailinator.com/v4/public/inboxes.jsp?to=testikabcd
                    "testikabcd@mailinator.com")
  ;; => [false "550-5.1.1 The email account that you tried to reach does not exist. Please try\n"]


  (.getHostName (java.net.InetAddress/getLocalHost))
  .)
