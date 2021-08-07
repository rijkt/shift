(ns shift.rule-test
  (:require [clojure.test :refer :all]
            [shift.rule :refer :all]))

; save original rule to know where to put variable
(def test-rules {#"^source1/(\w+)/(\w+)/?" ["source1/{name}/{season}" "/target1/{name}/{season}"]
                 #"^source2/(\w+)/?" ["source2/{name}" "/target2/{name}"]
                 #"^mixed/(\w+)/(\w+)/?" ["mixed/{b}/{a}" "order/{a}/{b}"]})

(deftest single-variable-path-tset
  (testing "Get a path with a single parameter"
    (is (= (get-target-path "source2/a" test-rules) "/target2/a"))))


(deftest two-variable-path-tset
  (testing "Get a path with two parameters"
    (is (= (get-target-path "source1/a/b" test-rules) "/target1/a/b"))))

(deftest superfluous-beginning-test
  (testing "Make sure no characters can be added to beginning of input"
    (is (nil? (get-target-path "aaaatest1/a/b" test-rules)))))

(deftest mixed-order-test
  (testing "Make sure variables order of variables in source and target path are irrelevant"
    (is (= (get-target-path "mixed/x/y" test-rules) "order/y/x"))))
