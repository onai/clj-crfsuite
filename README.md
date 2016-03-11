# clj-crfsuite

[![Circle CI](https://circleci.com/gh/onutech/clj-crfsuite.svg?style=svg&circle-token=351e60b226583e6e24fece5d35f03fbb4f50d3bc)](https://circleci.com/gh/onutech/clj-crfsuite)

Clojure interface to CRFSuite.

Allows you to express sequences as clojure maps à la pycrfsuite.

## Usage

### Lein artifact

[![Clojars Project](http://clojars.org/clj-crfsuite/latest-version.svg)](http://clojars.org/clj-crfsuite)



### Training:

Pass in a sequence of training examples, a sequence of tags / labels and a model filename to save to.

```clojure
user=> (train [[{:feat1 2.0, :feat2 3.0} {:feat1 3.0, :feat2 4.0}] [{:feat1 2.0, :feat2 3.0} {:feat1 4.0, :feat2 5.0}]] [["y1", "y2"], ["y1", "y3"]] "trainmodel.crfsuite")
.
.
.
L-BFGS resulted in convergence
Total seconds required for training: 0.000

Storing the model
user=>
```

You can also supply a vector of features (or an ndarray like `core.matrix`):

```clojure
      (train [
              [
               [1.0, 2.0],
               [3.0, 4.0]
               ]
              [
               [1.0, 2.0],
               [3.0, 4.0]
               ]
              ]
             
             [["y3", "y4"]
              ["y3", "y4"]]

             "trainmodel.crfsuite")
```

### Tagging:

Pass a sequence of features to be tagger with a model stored in `traimodel.crfsuite`. Returns a
sequence of tag, probability pairs.

```clojure
user=> (tag [{:feat1 2.0, :feat2 3.0} {:feat1 3.0, :feat2 4.0}] "trainmodel.crfsuite")
(["y1" 0.43526113123400284] ["y3" 0.36107961034023944])
```


## License

Copyright © 2015 - 2016 Onu Technology, Inc.

Distributed under the Apache v2 License.
