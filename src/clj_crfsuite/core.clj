(ns clj-crfsuite.core
  (:require [clojure.walk :refer [stringify-keys]])
  (:import [com.github.jcrfsuite CrfTagger CrfTrainer]
           [third_party.org.chokkan.crfsuite
            Attribute
            Item
            ItemSequence
            StringList]))

(defn to-item
  "Allows you to specify an item in the following formats:
   1. {:feat1 :val1, :feat2 :val2}
   2. {:feat1 2.0, :feat2 3.0}
   3. {:feat1 true, :feat2 3.0}"
  [a-map]
  (let [str-map (stringify-keys a-map)
        an-item (Item.)]
    (doseq [[k v] str-map]
      (println k)
      (let [attr (if (string? v)
                   (Attribute. (str k ":" v) 1.0)
                   (Attribute. k (double v)))]
        (.add an-item
              attr)))
    an-item))

(defn to-item-seq
  "A collection of maps is an itemsequence"
  [a-seq]
  (let [item-seq (ItemSequence.)]
    (doseq [a-map a-seq]
      (let [item (to-item a-map)]
        (.add item-seq
              item)))
    item-seq))

(defn to-string-list
  [a-str-vector]
  (let [s-list (StringList.)]
    (doseq [s a-str-vector]
      (.add s-list
            (name s)))
    s-list))

(defn train
  [x-seqs y-seqs model-file]
  (let [x-item-seqs (map to-item-seq x-seqs)
        y-str-seqs  (map to-string-list y-seqs)]
    (CrfTrainer/train x-item-seqs
                      y-str-seqs
                      model-file)))

(defn tag
  [x-seq model-file]
  (let [tagger      (CrfTagger. model-file)
        x-item-seq (to-item-seq x-seq)]
    (map (fn [a-pair]
           [(.first a-pair) (.second a-pair)])
         (.tag tagger x-item-seq))))
