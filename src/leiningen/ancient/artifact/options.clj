(ns leiningen.ancient.artifact.options
  (:require [leiningen.core
             [user :as user]]
            [leiningen.ancient.verbose :refer :all]
            [ancient-clj.core :as ancient]))

(defn- format-delta
  [start]
  (->> (quot (- (System/nanoTime) start) 1000000)
       (format "\u0394 %4dms")))

(defn- wrap-loaders
  "Wrap loaders with logging."
  [loaders]
  (->> (for [[repo-id f] loaders]
         (->> (fn [group id]
                (let [start (System/nanoTime)
                      vs (f group id)
                      tag (->> (vector
                                 (format "[%s]" repo-id)
                                 (format-delta start))
                               (apply format "%-9s - %-7s -"))]
                  (cond (instance? Throwable vs)
                        (warnf "%s failure when checking %s/%s: %s" tag group id vs)
                        (empty? vs)
                        (debugf "%s no results for %s/%s." tag group id)
                        :else
                        (debugf "%s %d versions found for %s/%s." tag (count vs) group id))
                  vs))
              (vector repo-id)))
       (into {})))

(defn prepare-repositories
  "Prepate the repositories for usage with ancient-clj."
  [repositories]
  (debugf "repositories: %s" (pr-str repositories))
  (-> (for [[k spec] repositories
            :when spec]
        (->> (if (fn? spec)
               spec
               (-> (if (string? spec) {:url spec} spec)
                   (user/profile-auth)
                   (user/resolve-credentials)))
             (vector k)))
      (ancient/create-loaders)
      (wrap-loaders)))

(defn- select-mirror
  "Select a mirror for the given repository name/URL."
  [mirrors name url]
  (some
    (fn [[match mirror]]
      (when (or (and (string? match) (#{name url} match))
                (and (instance? java.util.regex.Pattern match)
                     (some #(re-matches match %) [name url])))
        mirror))
    mirrors))

(defn select-mirrors
  [repositories mirrors]
  (->> (for [[name v] repositories
             :let [url (if (string? v) v (:url v))
                   mirror (select-mirror mirrors name url)]]
         [name (or mirror v)])
       (into {})))

(defn options
  "Prepare the option map."
  ([] (options {}))
  ([{:keys [repositories mirrors
            dependencies? plugins? profiles?
            snapshots? qualified?
            check-clojure?]
     :or {dependencies? true
          snapshots? false
          profiles? true
          qualified? false
          check-clojure? false}}]
   {:repositories (-> (or repositories ancient/default-repositories)
                      (select-mirrors mirrors)
                      (prepare-repositories))
    :snapshots? snapshots?
    :qualified? qualified?
    :check-clojure? check-clojure?
    :allowed-keys (->> (vector
                         (if dependencies? :dependencies)
                         (if plugins? :plugins)
                         (if profiles? :profiles))
                       (filter identity)
                       (set))
    :cache (ref {})}))
