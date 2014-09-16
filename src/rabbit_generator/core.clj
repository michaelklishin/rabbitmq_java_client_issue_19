(ns rabbit-generator.core
	(:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.exchange  :as le]
            [langohr.consumers :as lc]
            [langohr.basic     :as lb]
            [cheshire.core :as json]
            [clj-time.core :as t]
            [clj-time.format :as f]))

(def iso8601-time-format (f/formatters :date-time-no-ms))

(defn- generate-message [i]
	(json/generate-string {
		"type" "test",
		"@timestamp" (f/unparse iso8601-time-format (t/now))
		"message" "just a test"
		"ordinal" i
		}))

(defn -main
  []
  (let [conn (rmq/connect)
        ch (lch/open conn)
        exchange "logs"
        routing-key "logs.test"
        counter (atom 0)
        shutdown-fn (fn []
                      (println "shutting down, last sent " (str @counter))
                      (rmq/close ch)
                      (rmq/close conn))]
    (le/declare ch "logs" "topic" {:durable false})
    (.addShutdownHook (Runtime/getRuntime) (Thread. shutdown-fn))
    (try
      (println "starting generator")
      (doseq [i (range)]
        (lb/publish ch exchange routing-key (generate-message i)
                    {:persistent false :content-type "application/json" :mandatory false})
        (swap! counter inc))
      (catch Exception e
        (println (str "Exception: " (.getMessage e)))))))

