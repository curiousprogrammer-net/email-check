(ns net.curiousprogrammer.dns
  "DNS utilities, building upon excellent dnsjava library: https://github.com/dnsjava/dnsjava"
  (:import (org.xbill.DNS Lookup Type Name)))

(defn- to-clojure-maps
  "Converts an array of dns records returned  by the dnsjava library
  into a seq of clojure maps sorted by priority;
  most important ones (the lowest priority number) go first."
  [dns-records-array]
  (->> dns-records-array
       seq
       (map bean)
       (map #(update-vals % (fn [v] (if (instance? Name v)
                                      (str v)
                                      v))))
       (sort-by :priority)))

(defn mx-lookup!
  "Returns a sequence MX records (maps) for given email-domain sorted by priority
  (most important ones, that is _lowest_ priority number, first).

  Returns an empty sequence if there are no MX records for the domain."
  [email-domain]
  ;; Check javadoc for MXRecord, Record, Name classes: https://javadoc.io/doc/dnsjava/dnsjava/latest/index.html
  (-> (Lookup. email-domain Type/MX)
      .run
      to-clojure-maps))


(defn valid-mx-record
  "checks whether the domain has a valid mx record
  and if so, returns the record with highest priority.
  See https://mailtrap.io/blog/verify-email-address-without-sending/#DNS-lookup.
  Read more about 'priority' of MX records here: https://news.gandi.net/en/2021/06/why-mx-records-have-a-priority-and-a-and-ns-records-dont/"
  [email-domain]
  (first (mx-lookup! email-domain)))

(comment
  (mx-lookup! "google.com")
  ;; => ({:name "google.com.",
  ;;      :RRsetType 15,
  ;;      :type 15,
  ;;      :TTL 276,
  ;;      :priority 10,
  ;;      :class org.xbill.DNS.MXRecord,
  ;;      :additionalName "smtp.google.com.",
  ;;      :target "smtp.google.com.",
  ;;      :DClass 1})

  (mx-lookup! "gmail.com")
  ;; => ({:name "gmail.com.",
  ;;      :RRsetType 15,
  ;;      :type 15,
  ;;      :TTL 3096,
  ;;      :priority 5,
  ;;      :class org.xbill.DNS.MXRecord,
  ;;      :additionalName "gmail-smtp-in.l.google.com.",
  ;;      :target "gmail-smtp-in.l.google.com.",
  ;;      :DClass 1}
  ;;     {:name "gmail.com.",
  ;;      :RRsetType 15,
  ;;      :type 15,
  ;;      :TTL 3096,
  ;;      :priority 10,
  ;;      :class org.xbill.DNS.MXRecord,
  ;;      :additionalName "alt1.gmail-smtp-in.l.google.com.",
  ;;      :target "alt1.gmail-smtp-in.l.google.com.",
  ;;      :DClass 1}
  ;;    ...

  (time (mx-lookup! "abcdefghi.com"))
  ;; => ()
  ;; "Elapsed time: 85.097432 msecs"

  ;; lookup gmail.com when disconnected from the Internet
  ;; NOTE: once the result is cached it works even when disconnected
  (time (mx-lookup! "mailinator.com"))
  ;; => ()
  ;; You'll see in the logs: java.net.SocketException: Network is unreachable
  ;; "Elapsed time: 152.16588 msecs"

  .)
