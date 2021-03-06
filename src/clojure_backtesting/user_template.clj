(ns clojure-backtesting.user
  (:require [clojure.test :refer :all]
            [clojure-backtesting.data :refer :all]
            [clojure-backtesting.order :refer :all]
            [clojure-backtesting.evaluate :refer :all]
            [clojure.string :as str]
            ;;[clojure.set :as s]
            ))

(defn get-set
    "Return a list of datadate, need to be converted to set"
    [file2] ; COMPUSTAT
    (loop [remaining file2
            result-set []]
        (if (empty? remaining)
            result-set
            (let [first-line (first remaining)
                next-remaining (rest remaining)
                next-result-set (conj result-set (get first-line :datadate))]
            (recur next-remaining next-result-set)
            )
        )
    )
)

(defn last-quar
    "Return the last quarter date of a given row"
    [row]
    (let [date (get row :date)]
        (let [[year month day] (map parse-int (str/split date #"-"))]
            (cond
            (or (and (= month 12) (= day 31)) (= month 1) (= month 2) (and (= month 3) (<= day 30))) (str (- year 1) "-" 12 "-" 31)
            (or (and (= month 3) (= day 31)) (= month 4) (= month 5) (and (= month 6) (<= day 29))) (str year "-3-" 31)
            (or (and (= month 6) (= day 30)) (= month 7) (= month 8) (and (= month 9) (<= day 30))) (str year "-6-" 30)
            (or (and (= month 9) (= day 31)) (= month 10) (= month 11) (and (= month 12) (<= day 30))) (str year "-" 9 "-" 30))
        )
    )
)


(defn insert-col
    "Insert the date col from COMPUSTAT into CRSP"
    [file1 set] ;; key: before the insert col; file1: insert into this file; set: col to be inserted
    ;; add a thing in each map using key and value
    ;; run loop, length of the loop = length of the list
    (for [row file1] (concat row {:datadate (condp contains? (last-quar row) set (last-quar row) "")}))
)

(defn merge-data-row
    "merge 2 csv files "
    [file1 file2]
    
    (def f1 (read-csv-row file1)) ;;file 1 is CRSP
    (def f2 (read-csv-row file2)) ;;file 2 is COMPUSTAT
    
    (def f0 (insert-col f1 (into #{} (get-set f2)))) ;;insert datadate to file 1

    (left-join f0 f2 {:datadate :datadate :tic :TICKER})

    ;;(def file0 (insert-col file1 set))
    ;; need to parse-int later
)

(defn merge-data-col
    "merge 2 csv files based on the column model"
    [file1 file2]
    (row->col (merge-data-row file1 file2))
)

(defn -main
    "Write your code here"
    [& args]
    (println "hello world")
)
