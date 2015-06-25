(defn implode [v sep]
  (conj (vec (interpose sep v)) sep)
)

(defn saveurl [uri file]
  (with-open [in (clojure.java.io/input-stream uri)
              out (clojure.java.io/output-stream file)]
    (clojure.java.io/copy in out)))

(def tmp-dir "c:/tmp/slideshare-cache")

(defn saver [index urllist]
  (if (file-exist (str tmp-dir "/" index ".jpg"))
    (println (str index ".jpg") " existed, reuse it")
    (do
      (saveurl (nth urllist index ) (str tmp-dir "/" index ".jpg"))
      (println "mesg: " (str index ".jpg") " saved")
    )
  )
)

(defn get-pdfname [url]
  (str (.substring (second (re-find #"(.*)-[^-]*?$" (second (re-find #"(.*)-[^-]*?$"  (re-find #"\/[^\/]*?$" url))))) 1) ".pdf")
)

(defn clean-slideshare-cache []
  (del-dir tmp-dir "*.jpg")
)

(defn saveurl-concurrent-and-gen [urllist callback]
  (doall (map #(deref %) (doall (map-indexed #(future (saver (first [%1 %2]) urllist)) urllist))))
  (callback  (get-pdfname (first urllist)) urllist)
  (println "one pdf saved successfully, cleaning....")
  (clean-slideshare-cache)
)

(defn filelist [urllist] (map #(str %1 ".jpg") (take (count urllist) (iterate inc 0))))

(require 'clj-pdf.core)

(defn genpdf [pdfname urllist]
  (clj-pdf.core/pdf 
    [{
     :size ::crown-quarto}
     
     (let [imagelist (for [url  (filelist urllist)]
        [:image 
         {:align :center  ; this only horizontally center
         }
         (str tmp-dir "/" url)]
        )]
        (implode imagelist [:pagebreak]))
    ]
    (str tmp-dir "/" pdfname)
  )
)

(defn get-urllist [url]
  (let [ d (prototype.Downloader. )
         content (.DownloadPage d url)
         dbuilder (prototype.DOMBuilder.)
         w (.GetWalkerByDoc dbuilder (.GetDoc dbuilder content))
         ie (prototype.InfoExtracter.)
         selector ".slide .slide_image {}"
         items (.FindElements ie selector w url)
         urllist (map #(.getAttribute % "data-full") items) 
       ]
    ;;
    urllist
  )
)

(defn gen-pdf [url]
  (saveurl-concurrent-and-gen (get-urllist url) genpdf)
)


