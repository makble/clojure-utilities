(import '(com.itextpdf.text Document)
            '(com.itextpdf.text.pdf PdfReader PdfWriter PdfContentByte PdfImportedPage BaseFont PdfCopy)
            '(java.io File FileInputStream FileOutputStream InputStream OutputStream))

(defn copy-pdf-range [src dest page-start page-end]
  (let [doc (Document.)
        os (FileOutputStream. dest)
        copy (PdfCopy. doc os)
        reader (PdfReader. src)
       ]
    (doto doc (.open))
    (loop [page page-start]
      (if (> page page-end)
        nil
        (do
          (.addPage copy (.getImportedPage copy reader page))
          (recur (inc page))
        )
      )
    )
    (.close doc)
    (.close reader)
    (.close os)
  )
)
