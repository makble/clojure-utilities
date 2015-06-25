;; init script for REPL
(use 'clojure.contrib.math)

;; highest
(def join clojure.string/join)

;; this may change, we should not hard code it. 
(def bindir "c:\\bin")

(defn del [path]
  (let [ f (java.io.File. path)]
    (.delete f)
  )
)

;; init for nrepl
(require 'cider.nrepl)
(require 'clojure.tools.nrepl.server)


;; common tools

(def yahoo-prefix "https://search.yahoo.com/search;_ylt=AwrBTzr5QbVUN5UAFZSl87UF;_ylc=X1MDOTU4MTA0NjkEX3IDMgRmcgMEZ3ByaWQDZTYzRTFzU1hReHVUVEtMaTJqelFKQQRuX3JzbHQDMARuX3N1Z2cDMTAEb3JpZ2luA3NlYXJjaC55YWhvby5jb20EcG9zAzAEcHFzdHIDBHBxc3RybAMEcXN0cmwDOQRxdWVyeQNqb2huIHdpY2sEdF9zdG1wAzE0MjExNjUwNTA-?p=")

(defn query-to-urlparameter [query]
  (java.net.URLEncoder/encode query "UTF-8")
)

(defn decode-yahoo-url [url]
  (let [extracted (second (re-find #"\/RU=(.*)\/RK"  url)) ]
    (if (nil? extracted) url
      (java.net.URLDecoder/decode extracted  "UTF-8")
    )
  )
)

(defn yahoo-search [query]
  (let [ url (str yahoo-prefix (query-to-urlparameter query))
         first-page (prototype.Util/DownloadPage url)
         dbuilder (prototype.DOMBuilder.)
         w  (.GetWalkerByDoc dbuilder (.GetDoc dbuilder first-page))
         link-selector "h3 a.ac-algo {}"
         ie (prototype.InfoExtracter.)
         items (map #(hash-map (decode-yahoo-url (.getAttribute % "href")) (.getTextContent %)) (.BasicQueryFindLink ie link-selector w url))
       ]
    ;;(clojure.pprint/pprint items)
    items
  )
)

(defn yahoo [query]
  (clojure.pprint/pprint (yahoo-search query))
)


(defn md5 [s]
  (let [algorithm (java.security.MessageDigest/getInstance "MD5")
        size (* 2 (.getDigestLength algorithm))
        raw (.digest algorithm (.getBytes s))
        sig (.toString (java.math.BigInteger. 1 raw) 16)
        padding (apply str (repeat (- size (count sig)) "0"))]
    (str padding sig)))

; 调用windows cmd 命令
(defn shell-inner [cmd arg waitfn] 
  (let [
         p (.exec (Runtime/getRuntime) (str cmd " " arg))
         ip (.getInputStream p)
         ipr ( java.io.InputStreamReader. ip)
         br (java.io.BufferedReader. ipr)
         
         ip-error (.getErrorStream p)
         ipr-error ( java.io.InputStreamReader. ip-error)
         br-error (java.io.BufferedReader. ipr-error)
       ]
 
    (let [ return-output 
      (loop [str (.readLine br) output []]
        (if (nil? str)
          output
          (recur (.readLine br) (conj output str))
        )
      )
        ]
      
      ;; consume error stream to prevent process from hanging
      (loop [str (.readLine br-error)]        
        (if (nil? str)
          (do (waitfn p) "")
          (recur (.readLine br-error))
        )
      )
      return-output
    )
  )
)

(defn shell [cmd arg]
  (shell-inner cmd arg (fn [p] (.waitFor p)))
)

(defn shell-independent [cmd arg]
  (let [
         p (.exec (Runtime/getRuntime) (str cmd " " arg))
       ]
  )
)

 
(defn printlnv [v]
  (loop [i 0]
    (if (>= i (count v))
        ""
        (do
          (println (v i))
          (recur (inc i))
        )
    )
  )
)

(defn printlnv-lazy [v]
  (loop [i 0]
    (if (>= i (count v))
        ""
        (do
          (println (nth v i))
          (recur (inc i))
        )
    )
  )
)

(defn contains-whitespace [str] 
  (if (= 0 (count (clojure.string/replace str #"[^ ]" "")) )
    false
    true
  )
)

(defn add-double-quote [s]
  (if (contains-whitespace s)
    (str "\""  s "\"")
    s
  )
)


(defn del-dir [dir what-to-del]
  (shell "cmd /c del "   (str (.replace dir "/" "\\") "\\"  what-to-del))
)

(defn kb-to-gb [bytes] (/ bytes (* 1024.0 1024.0 )))

(defn byte-to-gb [bytes] (/ bytes (* 1024.0 1024.0 1024.0)))

(defn one-precision [f] (if (> (.length f ) 3) (.substring f 0 3) f))

(defn mem []
  (let [free-mem-f (Float/parseFloat (clojure.string/trim (nth (shell "wmic" "OS get FreePhysicalMemory") 2)))
        totoal-mem-f (Float/parseFloat (clojure.string/trim (nth (shell "wmic" "ComputerSystem get TotalPhysicalMemory") 2)))
        used-mem-f (- totoal-mem-f (* free-mem-f 1024.0))
        free-mem (java.lang.String/format "%.1f" (into-array  [(kb-to-gb free-mem-f)]))
        total-mem (java.lang.String/format "%.1f" (into-array [(byte-to-gb totoal-mem-f)]))
        used-mem (java.lang.String/format "%.1f" (into-array [(byte-to-gb used-mem-f)]))
       ]
    ;;(str "used: " used-mem "GB / total: " total-mem "GB / free: " free-mem "GB")
    (str  used-mem "GB / " total-mem "GB / " free-mem "GB")
  )
)

(defn get-p-name [line]
  (cond
    (.startsWith line "System Idle Process" ) "System Idle Process"      
    (.startsWith line "System" ) "System"
    :else (re-find #"^.*?\.exe" line)
  )
)

(defn kb-to-mb [bytes] (/ bytes (* 1024.0 1.0 )))

(defn kb-to-m [kb-str]
  (kb-to-mb (Float/parseFloat kb-str))
)

(defn get-p-mem [line]
  (kb-to-m (.replace (second (re-find #"([0-9,]*?) K" line)) "," ""))
)


(defn pretty-memp [v take-count]
  (join "\n" (take take-count (map #(str (first %) ": " (second %)) v) ))
)

(defn memp-inner []
   (reverse (map #(vec [(first %) (java.lang.String/format "%.1f M" (into-array [(second %)]))])
    (sort-by #(second %) (map #(vec [(get-p-name %) (get-p-mem %)]) (rest (rest (rest (shell "tasklist.exe" ""))))))
  ))
)

(defn memp 
  ([] (println (pretty-memp (memp-inner) 30)))
  ([take-count ] (println (pretty-memp (memp-inner) take-count)))
)

(defn read-file-as-lines [filename]
  (let [rdr (clojure.java.io/reader filename)]
    (loop [ret []]
      (if-let [line (.readLine rdr)]
        (recur (cons line ret))
        ret
      )
    )
  )
)

(def to-lower clojure.string/lower-case)

(defn read-file-as-text [file]
  (slurp file)
)


(defn uuid [] (.toString (java.util.UUID/randomUUID)))

(defn show [ t ] 
  (map #(println %) (seq (:declaredMethods (bean t)))  
  )  
)
 
(defn is-dir [path]
  (.isDirectory (clojure.java.io/file path))
)

(defn is-file [path]
  (.isFile (clojure.java.io/file path))
)

(def file-exist is-file)

(defn read-map-from-file [file]
  (read-string (read-file-as-text file))
)

(defn write-map-to-file [map file]
  (spit file (str map))
)

(defn urlencode [url]
  (java.net.URLEncoder/encode url "UTF-8")
)

(load-file "c:/codebase/clojure/exportpdf/copy.pdf.range.clj.el")
(load-file "c:/codebase/clojure/slideshare-pdf/slideshare-tool.clj.el")
(load-file "c:/codebase/clojure/googleimage/googleimage.prod.1.clj.el")

(clojure.tools.nrepl.server/start-server :port 7888 :handler cider.nrepl/cider-nrepl-handler)

