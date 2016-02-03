(ns clj-crfsuite.core-test
  (:require [clojure.test :refer :all]
            [clj-crfsuite.core :refer :all]))

(deftest run-test
  (testing "train and test calls must just-work"
    (try
      (is
       (nil?
        (train [
                [{:feat1 "2.0", :feat2 3.0}
                 {:feat1 3.0, :feat2 4.0}
                 ]
            
                [{:feat1 2.0, :feat2 3.0}
                 {:feat1 4.0, :feat2 5.0}
                 ]
                ]

               [
                ["y1", "y2"],
                ["y1", "y3"]
                ]
               "trainmodel.crfsuite")))

      (is
       (= [["y3" 0.3548467376927597]]
          (tag [{:feat1 2.0, :feat2 3.0}] "trainmodel.crfsuite")))
      (catch Exception e (is false)))))
