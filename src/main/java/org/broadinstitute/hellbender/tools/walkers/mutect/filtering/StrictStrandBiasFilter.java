package org.broadinstitute.hellbender.tools.walkers.mutect.filtering;

import htsjdk.variant.variantcontext.VariantContext;
import org.broadinstitute.hellbender.engine.ReferenceContext;
import org.broadinstitute.hellbender.tools.walkers.annotator.AnnotationUtils;
import org.broadinstitute.hellbender.utils.variant.GATKVCFConstants;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StrictStrandBiasFilter extends HardAlleleFilter<List<Integer>> {
    private final int minReadsOnEachStrand;

    public StrictStrandBiasFilter(final int minReadsOnEachStrand) {
        this.minReadsOnEachStrand = minReadsOnEachStrand;
    }

    @Override
    public ErrorType errorType() { return ErrorType.ARTIFACT; }

    @Override
    public List<Boolean> areAllelesArtifacts(final VariantContext vc, final Mutect2FilteringEngine filteringEngine, ReferenceContext referenceContext) {
        String sbStr = vc.getCommonInfo().getAttributeAsString(GATKVCFConstants.AS_SB_TABLE_KEY, null);
        if (sbStr == null) {
            return Collections.emptyList();
        }

        List<String> alleleSBs = AnnotationUtils.decodeAnyASListWithPrintDelim(sbStr);
        if (alleleSBs.size() <= 1) {
            return Collections.emptyList();
        }

        // skip the reference
        List<List<Integer>> sbs = alleleSBs.subList(1, alleleSBs.size()).stream().map(
                asb -> AnnotationUtils.decodeAnyASList(asb).stream()
                        .mapToInt(Integer::parseInt).boxed().collect(Collectors.toList())).collect(Collectors.toList());

        return sbs.stream().map(altList -> altList.stream().anyMatch(x -> x == 0)).collect(Collectors.toList());

    }

    @Override
    public String filterName() {
        return GATKVCFConstants.STRICT_STRAND_BIAS_FILTER_NAME;
    }

    @Override
    protected List<String> requiredAnnotations() { return Collections.emptyList(); }
}
