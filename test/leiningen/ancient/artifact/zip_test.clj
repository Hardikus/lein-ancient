(ns leiningen.ancient.artifact.zip-test
  (:require [midje.sweet :refer :all]
            [leiningen.ancient.utils :refer [with-temp-file]]
            [leiningen.ancient.artifact
             [check :as check]
             [zip :refer :all]])
  (:import [java.io StringWriter]))

(tabular
  (tabular
    (fact "about project file upgrading."
          (let [artifact (-> (check/read-artifact ?path ?artifact)
                             (assoc :latest {:version-string "0.1.1"}))
                [a _ & rst] ?artifact
                upgraded (reduce conj [a "0.1.1"] rst)
                contents (format ?fmt (pr-str ?artifact))
                expected (format ?fmt (pr-str upgraded))]
            (with-temp-file [f contents]
              (let [result (with-open [w (StringWriter.)]
                             (-> (?reader f)
                                 (upgrade-artifacts [artifact])
                                 (write-zipper! w))
                             (.toString w))]
                result =>  expected))))
    ?artifact
    '[artifact]
    '[artifact "0.1.0"]
    '[artifact "0.1.0" :exclusions [other]])
  ?fmt ?reader ?path
  (str "(defproject project-x \"0.1.1-SNAPSHOT\"\n"
       "  :dependencies [%s])")
  read-project-zipper!
  [:dependencies 0]

  (str "(defproject project-x \"0.1.1-SNAPSHOT\"\n"
       "  :dependencies [#_[ignore] %s])")
  read-project-zipper!
  [:dependencies 0]

  (str "{:prof {:plugins [[xyz \"0.2.0\"]%n"
       "                  %s]}}")
  read-profiles-zipper!
  [:prof :plugins 1])
