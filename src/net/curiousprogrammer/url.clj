(ns net.curiousprogrammer.url
  (:require
   [clojure.java.io :as jio]))

(defn fetch-as-stream
  "Opens stream for given URL and returns the open stream (see https://stackoverflow.com/a/5351894/1184752)
  `timeout` is applied as both connection and socket read timeout.)
  The client is responsible for closing the stream,
  via `.close` or `.disconnect` (see https://stackoverflow.com/questions/4767553/safe-use-of-httpurlconnection)"
  [url timeout]
  (let [url-connection (doto (.openConnection (java.net.URL. url))
                         (.setConnectTimeout timeout)
                         (.setReadTimeout timeout))]
    (.getInputStream url-connection)))

(defn fetch-as-string
  "As `fetch-as-stream` but converts the result to String
  and closes (disconnects) the URL input stream."
  [url timeout]
  ;; using StringWriter and `jio/copy` was inspired by `slurp`.
  (let [sw (java.io.StringWriter.)
        is (fetch-as-stream url timeout)]
    (try
      (jio/copy is sw)
      (.toString sw)
      (catch Exception _e (.disconnect is)))))

