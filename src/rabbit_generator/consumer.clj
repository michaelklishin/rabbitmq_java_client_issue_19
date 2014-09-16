(ns rabbit-generator.consumer
  (:require
   [clojure.string    :as s]
   [cheshire.core     :as json]
   [langohr.core      :as rmq]
   [langohr.channel   :as lch]
   [langohr.exchange  :as le]
   [langohr.consumers :as lc]
   [langohr.queue     :as lq]
   [langohr.basic     :as lb]
   [cheshire.core     :as json]
   [clj-time.core     :as t]
   [clj-time.format   :as f])
  (:import java.lang.management.ManagementFactory))

(defn my-pid
  [] 
  (let [^String name (-> (ManagementFactory/getRuntimeMXBean) .getName)]
    (Long/valueOf ^String (first (.split name "@")))))

(let [last-ack (atom 0)]
  (defn auto-ack-handler
    [ch metadata payload]
    (Thread/sleep 50))

  (defn manual-ack-handler
    [ch {:keys [delivery-tag] :as metadata} payload] 
    (let [n-acked (- delivery-tag @last-ack)]
      (Thread/sleep 50)
      (when (> n-acked 500)
        (do (lb/ack ch delivery-tag true)
            (println (format "acked %d deliveries" n-acked)))
        (reset! last-ack delivery-tag)))))

(defn -main
  [ack]
  (let [ack   (boolean (Boolean/parseBoolean ack))
        conn  (rmq/connect)
        ch    (lch/open conn)
        queue "test"]
    (lb/qos ch 1000)
    (lq/declare ch queue
                {:durable false :auto-delete false
                 :exclusive false :arguments {"x-max-length" 200000}})
    (lq/bind ch queue "logs" {:routing-key "#"})
    (let [on-message (if ack manual-ack-handler auto-ack-handler)
          consumer   (lc/create-default ch {:handle-delivery-fn on-message})]
      (println (format "Starting consumer, pid %d, manual ack mode? %b" (my-pid) ack))
      (lb/consume ch queue consumer {:auto-ack (not ack)}))))
