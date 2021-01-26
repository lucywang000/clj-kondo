(ns clj-kondo.impl.analyzer.match
  (:require [clj-kondo.impl.analyzer.common :as common]
            [clj-kondo.impl.utils :as utils]))

(defn vector-bindings [ctx expr]
  (let [children (:children expr)]
    (loop [children (seq children)
           bindings {}]
      (if children
        (let [child (first children)]
          (if (utils/symbol-token? child)
            (recur (next children)
                   (into bindings
                         (common/extract-bindings ctx child)))
            (do (common/analyze-expression** ctx child)
                (recur (next children)
                       bindings))))
        bindings))))

(defn analyze-match [ctx expr]
  (let [[_match pattern & clauses] (:children expr)]
    (common/analyze-expression** ctx pattern)
    (doseq [[clause ret] (partition 2 clauses)]
      (if (identical? :vector (utils/tag clause))
        (let [bindings (vector-bindings ctx clause)
              ctx (utils/ctx-with-bindings ctx bindings)]
          (common/analyze-expression** ctx ret))
        (do
          (common/analyze-expression** ctx clause)
          (common/analyze-expression** ctx ret))))))
