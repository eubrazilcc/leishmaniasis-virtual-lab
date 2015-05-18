#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
 Copyright 2014-2015 EUBrazilCC (EU‐Brazil Cloud Connect)

 Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 the European Commission - subsequent versions of the EUPL (the "Licence");
 You may not use this work except in compliance with the Licence.
 You may obtain a copy of the Licence at:

   http://ec.europa.eu/idabc/eupl

 Unless required by applicable law or agreed to in writing, software
 distributed under the Licence is distributed on an "AS IS" basis,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the Licence for the specific language governing permissions and
 limitations under the Licence.

 This product combines work with different licenses. See the "NOTICE" text
 file for details on the various modules and licenses.
 The "NOTICE" text file is part of the distribution. Any derivative works
 that you distribute must include a readable copy of the "NOTICE" text file.
"""

"""
Usage examples:

 1) Exports a phylogenetic tree to a SVG file, loading the tree from Newick a
    file, and including the sequences alignment from a FASTA file:

    tree-generator.py tree.nh alignment.fasta -o im.svg

 For additional support on ETE:

    http://etetoolkit.org/
    
 Possible integration in Java:
 
    http://mvnrepository.com/artifact/org.python/jython/2.7.0
"""

from argparse import ArgumentParser
import os.path
from ete2 import PhyloTree, TreeStyle

def is_valid_file(parser, arg):
    if not os.path.exists(arg):
        parser.error("The file %s does not exist!" % arg)
    else:
        return open(arg, 'r')  # return an open file handle

def load_tree(f_tree, f_align):
    # Tree style
    ts = TreeStyle()
    ts.show_leaf_name = True
    ts.show_branch_length = True
    ts.show_branch_support = True
    ts.branch_vertical_margin = 10    

    # Load phylogenetic tree
    tree = PhyloTree(f_tree.read())
    tree.link_to_alignment(f_align.read())
    return tree, ts

if __name__ == "__main__":
    # Parse input arguments
    parser = ArgumentParser()
    parser.add_argument("tree", help="tree", 
        type=lambda x: is_valid_file(parser, x))
    parser.add_argument("alignment", help="sequences alignment", 
        type=lambda x: is_valid_file(parser, x))
    parser.add_argument("-o", "--output", help="output file", 
        default="phylotree.svg")
    args = parser.parse_args()
    
    # Visualize the phylogenetic tree
    t, ts = load_tree(args.tree, args.alignment)
    ##t.show(tree_style=ts)
    t.render(args.output, tree_style=ts)
