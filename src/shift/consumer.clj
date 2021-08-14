(ns shift.consumer
  (:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.consumers :as lc]))

(defn- message-handler
  [ch {:keys [content-type delivery-tag type] :as meta} ^bytes payload]
  (println (format "[consumer] Received a message: %s, delivery tag: %d, content type: %s, type: %s, channel: %s,  meta %s"
                   (String. payload "UTF-8") delivery-tag content-type type (.getChannelNumber ch) meta)))

(defn- open-mq []
  (let [conn  (rmq/connect)
        ch    (lch/open conn)
        qname "shift"]
    (lq/declare ch qname {:auto-delete true})
    [conn ch qname]))

(defn start-consumer []
  (let [[conn ch qname] (open-mq)]
    (lc/subscribe ch qname message-handler {:auto-ack true})
    [conn ch]))

(defn stop-consumer [details]
  (let [[connection channel] details]
    (rmq/close channel)
    (rmq/close connection)))
