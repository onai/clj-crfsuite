(ns clj-crfsuite.core-test
  (:require [clojure.test :refer :all]
            [clj-crfsuite.core :refer :all]
            [me.raynes.fs :refer [delete]]))

(defn delete-models-fixture
  [f]
  (f)
  (delete "trainmodel.crfsuite"))

(use-fixtures :once delete-models-fixture)

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

(deftest types-test
  (testing "Are the various supported types conveniently?"
    (try
      (is
       (nil?
        (train [
                [{:feat1 "2.0", :feat2 3.0} ; string and double vals
                 {:feat1 3.0, :feat2 4.0}
                 ]
            
                [{:feat1 2.0, :feat2 3.0}   ; all double
                 {:feat1 4.0, :feat2 5.0}
                 ]
                

                [
                 {:feat1 true, :feat2 false} ; all boolean
                 {:feat1 false, :feat2 true}
                 ]]

               [
                ["y1", "y2"],
                ["y1", "y3"]
                ["y4", "y5"]]
               "trainmodel.crfsuite")))

      (is
       (not
        (nil?
         (tag [{:feat1 true, :feat2 true}] "trainmodel.crfsuite"))))

      (catch Exception e (is false)))))
