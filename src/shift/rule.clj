(ns shift.rule
  (:require [clojure.string :as s]
            [clojure.data.json :as json]))

(defn path-matches?
  "Checks if given source path matches exactly one rule"
  [path rules]
  (let [matches (map #(re-seq % path) (keys rules))
        normalized (remove nil? matches)]
    (= (count normalized) 1)))

(defn- get-params [path]
  (map first (re-seq #"\{([^}]+)\}" path)))

(defn- make-param-mapping
  "Decides how to replace variables in rules with actual parameters"
  [rule-source actual-params]
  (let [source-params (get-params rule-source)]
    (flatten (seq (zipmap source-params actual-params)))))

(defn- replace-all [content replacements]
  (reduce #(apply s/replace %1 %2) content (partition 2 replacements)))

(defn get-target-path
  "Derives copy target path from configured rule set"
  [actual-path rules]
  (let [matches (map (fn [rule] [(re-seq rule actual-path) (get rules rule)]) (keys rules))
        normalized (first (remove (comp nil? first) matches))]
    (if (not (nil? normalized))
      (let [[source & params] (ffirst  normalized) ; todo: make sure it's size 1
            [rule-source rule-target] (fnext normalized)
            replacements (make-param-mapping rule-source params)]
        (replace-all rule-target replacements))
      nil))) ; in case of no matches

(defn- add-replacements [params]
  (let [pattern  "(\\w+)"]
    (conj (vec (interpose pattern params)) pattern)))

(defn- construct-regex
  [source]
  (let [params (get-params source)
        replacements (add-replacements params)
        regex (replace-all source replacements)]
    (re-pattern regex)))

(defn load-rules!
  "Returns map with entries of the structure regex -> [source target]
  Takes a path to a json file with rules. Rules are json key-value pairs
  in the format source: target. Keys and values can contain variables
  like {name}."
  [file]
  (let [json (json/read-str (slurp file))
        source-target (first (partition 2 (seq json)))]
    (into {} (map (fn [pair] [(construct-regex (first pair)) pair]) source-target))))
