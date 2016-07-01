(set-env!
 :source-paths #{"src"}
 :dependencies '[[alandipert/boot-yeti             "1.0.0-1" :scope "test"]
                 [alandipert/boot-trinkets         "2.0.0"   :scope "test"]
                 [alandipert/yeti-lib              "0.9.9.1" :scope "runtime"]
                 [us.bpsm/edn-java                 "0.4.6"   :scope "runtime"]
                 [org.apache.commons/commons-lang3 "3.4"     :scope "runtime"]])

(require '[alandipert.boot-yeti :refer [yeti]]
         '[alandipert.boot-trinkets :refer [run]]
         '[clojure.java.io :as io]
         '[boot.util :as util]
         '[boot.core :as core]
         '[boot.pod :as pod])

(def +version+ "1.0.0")

(core/deftask string-replace
  "Replace strings in sources."
  [p path PATH str "Path to replace in."
   s subs SUBS {str str} "Strings to replace"
   r role ROLE kw "Input role of file after substitution, :resource or :source (default: :source)"]
  (let [tmp (core/tmp-dir!)]
    (core/with-pre-wrap [fs]
      (if-let [f (first (core/by-path [path] (core/input-files fs)))]
        (do
          (util/info "Replacing %s in %s...\n" subs path)
          (core/empty-dir! tmp)
          (spit
           (doto (io/file tmp path) io/make-parents)
           (reduce-kv #(.replace %1 %2 %3) (slurp (tmp-file f)) subs))
          (-> fs
              (core/rm [f])
              ((if (#{nil :source} role) core/add-source core/add-resource) tmp)
              core/commit!))
        fs))))

(core/deftask proguard
  [c config     PATH str "ProGuard config fileset-relative path"
   i input-jar  NAME str "input jar name"
   o output-jar NAME str "output jar name"]
  (let [tmp (core/tmp-dir!)
        pod (->> '[net.sf.proguard/proguard-base "5.2.1"]
                 (update pod/env :dependencies (fnil conj []))
                 pod/make-pod
                 future)]
    (with-pre-wrap [fs]
      (let [cfg    (first (core/by-path [config] (core/input-files fs)))
            in-jar (first (core/by-name [input-jar] (core/input-files fs)))
            libs   (interleave (repeat "-libraryjars") (.split (System/getProperty "boot.class.path") ":"))
            args   (into [(str "@"       (.getAbsolutePath (tmp-file cfg)))
                          "-injars"      (.getAbsolutePath (tmp-file in-jar))
                          "-outjars"     (.getAbsolutePath (io/file tmp output-jar))
                          "-libraryjars" "<java.home>/lib/rt.jar"]
                         libs)]
        (core/empty-dir! tmp)
        (util/info "Running ProGuard...\n")
        (util/without-exiting
         (pod/with-eval-in @pod (proguard.ProGuard/main (into-array ~args))))
        (-> fs
            (core/add-resource tmp)
            core/commit!)))))

(deftask build
  []
  (comp (string-replace :path "e2j/core.yeti"
                        :subs {"$VERSION$" +version+}
                        :role :source)
        (yeti)
        (uber)
        (jar :file "e2j.jar" :main 'e2j.Main)))

(deftask package
  []
  (comp (proguard :config "config.pro"
                  :input-jar "e2j.jar"
                  :output-jar "e2j-optimized.jar")
        (sift :include #{#"\.jar$"})
        (target)))
