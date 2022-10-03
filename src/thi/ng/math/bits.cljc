(ns thi.ng.math.bits
  (:refer-clojure :exclude [bit-count]))

(def ^:const INT-BITS 32)
(def ^:const INT-BITS1 (dec INT-BITS))
(def ^:const INT-MAX 0x7fffffff)
(def ^:const INT-MIN (bit-shift-left -1 INT-BITS1))

(def ^:const LOG2 (Math/log 2.0))

(defn abs
  [x]
  (let [mask (bit-shift-right x INT-BITS1)]
    (- (bit-xor x mask) mask)))

(defn log2?
  [x] (and (zero? (bit-and x (dec x))) (not (zero? x))))

(defn log2
  [x]
  (let [[x r] (if (> x 0xffff) [(unsigned-bit-shift-right x 16) 16] [x 0])
        [x r] (if (> x 0xff)   [(unsigned-bit-shift-right x 8) (bit-or r 8)] [x r])
        [x r] (if (> x 0xf)    [(unsigned-bit-shift-right x 4) (bit-or r 4)] [x r])
        [x r] (if (> x 0x3)    [(unsigned-bit-shift-right x 2) (bit-or r 2)] [x r])]
    (bit-or r (bit-shift-right x 1))))

(defn log10
  [x]
  (if (>= x 1000000000)
    9 (if (>= x 100000000)
        8 (if (>= x 10000000)
            7 (if (>= x 1000000)
                6 (if (>= x 100000)
                    5 (if (>= x 10000)
                        4 (if (>= x 1000)
                            3 (if (>= x 100)
                                2 (if (>= x 10)
                                    1 0))))))))))

(defn trailing-zeros
  [x]
  (let [x (bit-and x (- x))
        c (if (pos? x) 31 32)
        c (if (pos? (bit-and x 0x0000ffff)) (- c 16) c)
        c (if (pos? (bit-and x 0x00ff00ff)) (- c 8) c)
        c (if (pos? (bit-and x 0x0f0f0f0f)) (- c 4) c)
        c (if (pos? (bit-and x 0x33333333)) (- c 2) c)]
    (if (pos? (bit-and x 0x55555555)) (dec c) c)))

(defn ceil-pow2
  [x]
  (if (zero? x)
    1
    (let [x (dec x)
          x (bit-or x (unsigned-bit-shift-right x 1))
          x (bit-or x (unsigned-bit-shift-right x 2))
          x (bit-or x (unsigned-bit-shift-right x 4))
          x (bit-or x (unsigned-bit-shift-right x 8))
          x (bit-or x (unsigned-bit-shift-right x 16))]
      (inc x))))

(defn floor-pow2
  [x]
  (let [x (bit-or x (unsigned-bit-shift-right x 1))
        x (bit-or x (unsigned-bit-shift-right x 2))
        x (bit-or x (unsigned-bit-shift-right x 4))
        x (bit-or x (unsigned-bit-shift-right x 8))
        x (bit-or x (unsigned-bit-shift-right x 16))]
    (- x (unsigned-bit-shift-right x 1))))

(defn bit-count
  [x]
  (let [x (- x (bit-and (bit-shift-right x 1) 0x55555555))
        x (+ (bit-and x 0x33333333) (bit-and (bit-shift-right x 2) 0x33333333))]
    (bit-shift-right
     (* (bit-and (+ x (bit-shift-right x 4)) 0xf0f0f0f)
        0x1010101) 24)))

(defn parity
  [x]
  (let [x (bit-xor x (unsigned-bit-shift-right x 16))
        x (bit-xor x (unsigned-bit-shift-right x 8))
        x (bit-xor x (unsigned-bit-shift-right x 4))]
    (bit-and (unsigned-bit-shift-right 0x6996 (bit-and x 0xf)) 1)))

(defn even-parity? [x] (zero? (parity x)))
(defn odd-parity? [x] (pos? (parity x)))

(defn- interleave2*
  [x]
  (let [x (bit-and x 0xffff)
        x (bit-and (bit-or x (bit-shift-left x 8)) 0x00ff00ff)
        x (bit-and (bit-or x (bit-shift-left x 4)) 0x0f0f0f0f)
        x (bit-and (bit-or x (bit-shift-left x 2)) 0x33333333)]
    (bit-and (bit-or x (bit-shift-left x 1)) 0x55555555)))

(defn- interleave3*
  [x]
  (let [x (bit-and x 0x3fff)
        x (bit-and (bit-or x (bit-shift-left x 16)) 0xff0000ff)
        x (bit-and (bit-or x (bit-shift-left x 8)) 0xf00f00f)
        x (bit-and (bit-or x (bit-shift-left x 4)) 0xc30c30c3)]
    (bit-and (bit-or x (bit-shift-left x 2)) 0x49249249)))

(defn interleave2
  [x y]
  (bit-or (interleave2* x) (bit-shift-left (interleave2* y) 1)))

(defn interleave3
  [x y z]
  (bit-or
   (bit-or
    (interleave3* x)
    (bit-shift-left (interleave3* y) 1))
   (bit-shift-left (interleave3* z) 2)))

(defn deinterleave2-nth
  [x i]
  (let [x (bit-and (unsigned-bit-shift-right x i) 0x55555555)
        x (bit-and (bit-or x (unsigned-bit-shift-right x 1)) 0x33333333)
        x (bit-and (bit-or x (unsigned-bit-shift-right x 2)) 0x0f0f0f0f)
        x (bit-and (bit-or x (unsigned-bit-shift-right x 4)) 0x00ff00ff)
        x (bit-and (bit-or x (unsigned-bit-shift-right x 16)) 0x000ffff)]
    (bit-shift-right (bit-shift-left x 16) 16)))

(defn deinterleave2
  [x]
  [(deinterleave2-nth x 0)
   (deinterleave2-nth x 1)])

(defn deinterleave3-nth
  [x i]
  (let [x (bit-and (unsigned-bit-shift-right x i) 0x49249249)
        x (bit-and (bit-or x (unsigned-bit-shift-right x 2)) 0xc30c30c3)
        x (bit-and (bit-or x (unsigned-bit-shift-right x 4)) 0xf00f00f)
        x (bit-and (bit-or x (unsigned-bit-shift-right x 8)) 0xff0000ff)
        x (bit-and (bit-or x (unsigned-bit-shift-right x 16)) 0x3ff)]
    (bit-shift-right (bit-shift-left x 22) 22)))

(defn deinterleave3
  [x]
  [(deinterleave3-nth x 0)
   (deinterleave3-nth x 1)
   (deinterleave3-nth x 2)])

(defn next-combination
  [x]
  (let [t (bit-or x (dec x))]
    (bit-or
     (inc t)
     (unsigned-bit-shift-right
      (dec (bit-and (bit-not t) (- (bit-not t))))
      (inc (trailing-zeros x))))))
