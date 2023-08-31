package com.busted_moments.core.time;

public interface FormatFlag {
   void apply(Duration.Formatter formatter);

   FormatFlag COMPACT = formatter -> formatter.SUFFIX_GETTER = (ignored, unit) -> unit.getSuffix() + " ";

   FormatFlag MINIFIED = formatter -> formatter.SUFFIX_GETTER = (ignored, unit) -> unit.getSuffix();
}
