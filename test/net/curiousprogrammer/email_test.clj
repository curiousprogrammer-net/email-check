(ns net.curiousprogrammer.email-test
  (:require [net.curiousprogrammer.email :as email]
            [clojure.test :refer [are deftest testing]]))


(deftest validate-email
  (testing "Valid emails"
    (are [email] (nil? (email/validate email))
      "john@example.com"
      "john.doe@example.com"
      "John.Doe123@example.co.uk"
      "john.doe+123@gmail.com"))
  (testing "Invalid emails"
    (are [email] (string? (email/validate email))
      "@example.com"
      "john.doe@"
      "j@com"
      "john@example@example.com"
      nil
      ""
      "   "
      ;; tab
      "	")))

(deftest disposable-emails
  (testing "Proper emails"
    (are [email] (not (email/disposable? email))
      "john@example.com"
      "john.doe@example.com"
      "John.Doe123@example.co.uk"
      "john.doe+123@gmail.com")) (testing "Disposable emails"
    (are [email] (email/disposable? email)
      "testik@mailinator.com")))


;; (deftest verify-email
;;   (testing "disposable emails are invalid"
;;     (is (= (email/verify! "testik@mailinator.com"))))
;;   )
