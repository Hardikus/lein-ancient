(ns leiningen.version-test
  (:use midje.sweet
        leiningen.ancient.version))

(tabular
  (fact "about version map creation"
    (let [m (version-map ?version)]
      (:version m) => ?v
      (:version-str m) => ?version))
  ?version         ?v
  "1.0.0"          [1 0 0]
  "1.0"            [1 0]
  "1"              [1]
  "1a"             [1 "a"]
  "1-a"            [1 ["a"]]
  "1.0.1-SNAPSHOT" [1 0 1 ["snapshot"]]
  "1.0.1-alpha2"   [1 0 1 ["alpha" 2]]
  "11.2.0.3.0"     [11 2 0 3 0])

(fact "about SNAPSHOTs"
  (version-map "1.0.0") =not=> snapshot?
  (version-map "1.0.0-SNAPSHOT") => snapshot?)

(fact "about qualified versions"
  (version-map "1.0.0") =not=> qualified?
  (version-map "1.0.0-SNAPSHOT") => qualified?
  (version-map "1.0.0-alpha1") => qualified?
  (version-map "1.0.0-1-2") =not=> qualified?
  (version-map "1.0.0-1-2-coala") => qualified?)

(tabular
  (fact "about version comparison"
    (version-compare ?v0 ?v1) => ?r)
  ?v0              ?v1               ?r
  ;; Numeric Comparison
  "1.0.0"          "1.0.0"           0
  "1.0.0"          "1.0"             0
  "1.0.1"          "1.0"             1
  "1.0.0"          "1.0.1"          -1
  "1.0.0"          "0.9.2"           1
  "0.9.2"          "0.9.3"          -1
  "0.9.2"          "0.9.1"           1
  "0.9.5"          "0.9.13"         -1
  "10.2.0.3.0"     "11.2.0.3.0"     -1
  "10.2.0.3.0"     "5.2.0.3.0"       1
  "1.0.0-SNAPSHOT" "1.0.1-SNAPSHOT" -1
  "1.0.0-alpha"    "1.0.1-beta"     -1
  "1.1-dolphin"    "1.1.1-cobra"    -1

  ;; Lexical Comparison
  "1.0-alpaca"     "1.0-bermuda"    -1
  "1.0-alpaca"     "1.0-alpaci"     -1
  "1.0-dolphin"    "1.0-cobra"       1

  ;; Qualifier Comparison
  "1.0.0-alpha"    "1.0.0-beta"     -1
  "1.0.0-beta"     "1.0.0-alpha"     1
  "1.0.0-alpaca"   "1.0.0-beta"     -1
  "1.0.0-final"    "1.0.0-milestone" 1

  ;; Qualifier/Numeric Comparison
  "1.0.0-alpha1"   "1.0.0-alpha2"   -1
  "1.0.0-alpha5"   "1.0.0-alpha23"  -1
  "1.0-RC5"        "1.0-RC20"       -1
  "1.0-RC11"       "1.0-RC6"         1

  ;; Releases are newer than SNAPSHOTs
  "1.0.0"          "1.0.0-SNAPSHOT"  1
  "1.0.0-SNAPSHOT" "1.0.0-SNAPSHOT"  0
  "1.0.0-SNAPSHOT" "1.0.0"          -1

  ;; Releases are newer than qualified versions
  "1.0.0"          "1.0.0-alpha5"    1
  "1.0.0-alpha5"   "1.0.0"          -1

  ;; SNAPSHOTS are newer than qualified versions
  "1.0.0-SNAPSHOT" "1.0.0-RC1"       1
  "1.0.0-SNAPSHOT" "1.0.1-RC1"      -1
  
  ;; Some other Formats
  "9.1-901.jdbc4"   "9.1-901.jdbc3"   1
  "9.1-901-1.jdbc4" "9.1-901.jdbc4"   1

  )