(ns shift.rule
  (:require [clojure.string :as s]))
                                        ; todo: define format
                                        ; todo: read rules from file

(defn path-matches?
  "Checks if given source path matches exactly one rule"
  [path rules]
  (let [matches (map #(re-seq % path) (keys rules))
        normalized (remove nil? matches)]
    (= (count normalized) 1)))

(defn- make-param-mapping
  "Decides how to replace variables in rules with actual parameters"
  [rule-source actual-params]
  (let [source-params (map first (re-seq #"\{([^}]+)\}" rule-source))]
    (flatten (seq (zipmap source-params actual-params)))))

(defn get-target-path
  "Derives copy target path from configured rule set"
  [actual-path rules]
  (let [matches (map (fn [rule] [(re-seq rule actual-path) (get rules rule)]) (keys rules))
        normalized (first (remove (comp nil? first) matches))]
    (if (not (nil? normalized))
      (let [[source & params] (ffirst  normalized) ; todo: make sure it's size 1e
            [rule-source rule-target] (fnext normalized)
            replacements (make-param-mapping rule-source params)]
        (reduce #(apply s/replace %1 %2) rule-target (partition 2 replacements)))
      nil))) ; in case of no matches
