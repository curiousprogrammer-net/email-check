(ns net.curiousprogrammer.email
  "Basic utilities for checking emails.
  The core function is `verify!` which can perform multiple checks depending on the options.
  You can also call `validate` (basic syntax check) and `disposable?` separately.

  Resources
  - email regex-based validator: https://github.com/lamuria/email-validator
  - info about browsers and the 'email' input type: https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/email#validation"
  (:require
   [clojure.string :as str]
   [net.curiousprogrammer.dns :as dns]
   [net.curiousprogrammer.url :as url]))

;; see https://github.com/lamuria/email-validator/blob/master/src/email_validator/core.clj
(def email-regex #"(?i)[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")

(defn validate
  "A basic validation of an email - syntactical check.
  For a more thorough check, see `verify!`.

  Returns nil if the email is valid, error message otherwise."
  [email]
  (cond
    (or (empty? email) (str/blank? email)) "Email must not be empty"
    (nil? (re-matches email-regex email)) "Invalid email format"))


;; see https://github.com/disposable/disposable-email-domains
(def disposable-domains (delay (set (str/split-lines
                                     (try (url/fetch-as-string "https://disposable.github.io/disposable-email-domains/domains.txt"
                                                               5000)
                                          ;; nothing if it times out
                                          (catch Exception _e ""))))))

(defn email-domain [email]
  (when-not (validate email)
    (second (.split email "@"))))

(defn disposable-domain?
  "Checks the email domain against the predefined list of disposable email domains
  available at https://disposable.github.io/disposable-email-domains/.
  The list is loaded only once and then cached afterward throughout the lifetime of the process."
  [email-domain]
  (contains? @disposable-domains email-domain))

(defn disposable?
  "Checks if given email is a disposable email address.
  See https://blog.kickbox.com/what-is-a-disposable-email-address/

  See also `disposable-domain?`."
  [email]
  (disposable-domain? (email-domain email)))

(defn verify!
  "More thorough email validation (aka \"verification\") including:
  - Syntax validation (same as `validate`)
  - Check for disposable emails (ala mailinator.com)
  - DNS lookup (via dnsjava lib: https://github.com/dnsjava/dnsjava/blob/master/EXAMPLES.md)
  - Email box ping (disabled by default - DOES NOT WORK YET with modern JDK versions)

  Options can be specified as a final hashmap-like argument:
  ---------------------------------------------------------
  1. `:check-disposable` (default: true) -  checks that the email domain is not disposable like `mailinator.com`.
  2. `:check-mx-record` (default: true) -  checks that the email domain has a valid mx record.
  3. `:check-recipient` (default: false) -  tries to perform real SMTP transaction with the mx server
      to determine if the recipient's email address is valid.
      It requires `:check-mx-record` to be `true`.
      It's `false` by default since it involves a full SMTP transaction which is more prone to failures
      than the other simpler checks.
      The `check-recipient-mail-from` parameter is used for in `check-recipient!` for the 'MAIL FROM' smtp param.
      It defaults to 'no-reply@example.com'.

  Returns a map structure describing the validation result:
  - `:valid?` - overall result of the validation, true or false; depends on the options
  - `:email-domain` - domain derived from the given email
  - `:format-error` - string description of a syntactic error with given email address as per `validate`,
                      or `nil` of the format is valid
  - `:disposable` -  true if the email's domain is disposable (such as mailinator.com),
                     false if the domain is not disposable,
                     :unknown if it couldn't be checked (due to a network error such as request timeout)
  - `:mail-server` - the mail server (if any) retrieved via the email domain's MX record
                     or :unknown if the `check-mx-record` was set to false
  - `:recipient-error` - the error (if any) received during `check-recipient` smtp verification process

  See also https://mailtrap.io/blog/verify-email-address-without-sending/
  for more information about the whole email validation & verification process."
  [email & {:keys [check-disposable check-mx-record check-recipient check-recipient-mail-from]
            :or {check-disposable true check-mx-record true check-recipient false
                 check-recipient-mail-from "no-reply@example.com"}
            :as _options}]
  (assert (or (not check-recipient)
              (and check-recipient check-mx-record))
          "If check-recipient is true, you must also set check-mx-record to true!")

  (let [domain (email-domain email)
        disposable (if (and domain check-disposable)
                     (disposable-domain? domain)
                     :unknown)
        mail-server (if (and domain check-mx-record)
                      (:target (dns/valid-mx-record domain))
                      :unknown)
        recipient (if (and domain check-recipient)
                    ;; add a level of indirection through requiring-resolve because smtp functionality might not be needed
                    ;; and it could load unnecessary dependencies such as internal JDK classes
                    (let [check-recipient-fn (requiring-resolve 'net.curiousprogrammer.smtp/check-recipient!)]
                      (some-> mail-server (check-recipient-fn check-recipient-mail-from email)))
                    :unknown)
        recipient-error (if (map? recipient)
                          (:error recipient)
                          :unknown)]
    {:valid? (->> [domain (not disposable) mail-server recipient-error]
                  (remove #{:unknown})
                  (every? identity))
     :format-error (validate email)
     :email-domain domain
     :email-format-error (validate email)
     :disposable? disposable
     :mail-server mail-server
     :recipient-error recipient-error}))


;; email verification
(comment
  (disposable? "testik@mailinator.com")
  ;; => true

  (verify! "sales@codescene.com")
  ;; => {:valid? true,
  ;;     :email-domain "codescene.com",
  ;;     :disposable false,
  ;;     :mail-server "aspmx.l.google.com.",
  ;;     :recipient-error :unknown}

  ;; invalid MX record
  (verify! "testikabcd@non-existent-email-domain.com" :check-recipient false)
  ;; => {:valid? false, :email-domain "non-existent-email-domain.com", :disposable false, :mail-server nil, :recipient-error :unknown}

  ;; disposable email domain
  (verify! "testikabcd@mailinator.com")
  ;; => {:valid? false, :email-domain "mailinator.com", :disposable true, :mail-server "mail2.mailinator.com.", :recipient-error :unknown}

  ;;; TODO: full email verification only works for JDK 8
  (verify! "sales@codescene.com" :check-recipient true)
  ;; => {:valid? true, :email-domain "codescene.com", :disposable? false, :mail-server "aspmx.l.google.com.", :recipient-error nil}

  (verify! "not.exists@codescene.com" :check-recipient true)
  ;; => {:valid? false, :email-domain "codescene.com", :disposable? false, :mail-server "aspmx.l.google.com.", :recipient-error "550-5.1.1 The email account that you tried to reach does not exist. Please try\n"}

  (verify! "testikabcd@example.com")

  )


