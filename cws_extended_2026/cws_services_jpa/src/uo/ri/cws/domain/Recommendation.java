package uo.ri.cws.domain;

import uo.ri.cws.domain.base.BaseEntity;
import uo.ri.util.assertion.ArgumentChecks;

public class Recommendation extends BaseEntity {
    // Natural attributes
    private boolean usedForVoucher = false;

    // Accidental attributes
    private Client recommender;
    private Client recommended;

    public Recommendation(boolean usedForVoucher, Client recommender, Client recommended) {
        ArgumentChecks.isNotNull(recommender, "Recommender client cannot be null");
        ArgumentChecks.isNotNull(recommended, "Recommended client cannot be null");
        this.usedForVoucher = usedForVoucher;
    }

    public Recommendation(Client recommender, Client recommended) {
        this(false, recommender, recommended);
    }

    public boolean isUsedForVoucher() {
        return usedForVoucher;
    }

    public Client getRecommender() {
        return recommender;
    }

    public Client getRecommended() {
        return recommended;
    }

    void _setRecommender(Client recommender) {
        this.recommender = recommender;
    }

    void _setRecommended(Client recommended) {
        this.recommended = recommended;
    }

    public void markAsUsedForVoucher() {
        if (recommender.isEligibleForVoucher25() && !usedForVoucher) {
            usedForVoucher = true;
        }
    }

}
