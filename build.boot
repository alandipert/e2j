(set-env!
 :source-paths #{"src"}
 :dependencies '[[alandipert/boot-yeti             "1.0.0-1" :scope "test"]
                 [alandipert/boot-trinkets         "2.0.0"   :scope "test"]
                 [alandipert/yeti-lib              "0.9.9.1" :scope "runtime"]
                 [us.bpsm/edn-java                 "0.4.6"   :scope "runtime"]
                 [org.apache.commons/commons-lang3 "3.4"     :scope "runtime"]])

(require '[alandipert.boot-yeti :refer [yeti]]
         '[alandipert.boot-trinkets :refer [run]])

(deftask build
  []
  (comp (yeti)
        (uber)
        (jar :file "e2j.jar" :main 'e2j.Main)))

(deftask package
  []
  (comp (sift :include #{#"\.jar$"})
        (target)))
