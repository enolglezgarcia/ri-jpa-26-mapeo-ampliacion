package uo.ri.cws.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import uo.ri.cws.domain.base.BaseEntity;
import uo.ri.cws.domain.types.Address;
import uo.ri.cws.domain.types.Email;
import uo.ri.cws.domain.types.FullName;
import uo.ri.cws.domain.types.Nif;
import uo.ri.cws.domain.types.PhoneNumber;
import uo.ri.util.assertion.ArgumentChecks;
import uo.ri.util.assertion.StateChecks;

@Entity
@Table(name = "TCLIENTS")
public class Client extends BaseEntity {

    // Natural attributes
    @AttributeOverride(name = "nif", column = @Column(unique = true))
    private Nif nif;
    private FullName fullName;
    private Email email;
    private PhoneNumber phone;
    private Address address;

    // Accidental attributes
    @OneToMany(mappedBy = "client")
    private Set<Vehicle> vehicles = new HashSet<>();
    @OneToMany(mappedBy = "client")
    private Set<PaymentMean> paymentMeans = new HashSet<>();
    @OneToOne(mappedBy = "recommender")
    private Recommendation recommendedBy;
    @OneToMany(mappedBy = "recommended")
    private Set<Recommendation> recommendations = new HashSet<>();

    // ---- Constructors ----
    Client() {

    }

    public Client(Nif nif) {
        this(nif, new FullName("No name", "No surname"), new Email("default@example.com"),
            new PhoneNumber("+34600000000"), new Address("No street", "No city", "No zip", "No country"));
    }

    public Client(Nif nif, FullName fullName) {
        this(nif, fullName, new Email("default@example.com"), new PhoneNumber("+34600000000"),
            new Address("No street", "No city", "No zip", "No country"));
    }

    public Client(Nif nif, FullName fullName, Email email, PhoneNumber phone, Address address) {
        ArgumentChecks.isNotNull(nif, "Client NIF cannot be null");
        ArgumentChecks.isNotNull(fullName, "Client full name cannot be null");
        ArgumentChecks.isNotNull(email, "Client email cannot be null");
        ArgumentChecks.isNotNull(phone, "Client phone number cannot be null");
        ArgumentChecks.isNotNull(address, "Client address cannot be null");
        this.nif = nif;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public Nif getNif() {
        return nif;
    }

    public FullName getFullName() {
        return fullName;
    }

    public Email getEmail() {
        return email;
    }

    public PhoneNumber getPhone() {
        return phone;
    }

    public Address getAddress() {
        return address;
    }

    public Recommendation getRecommendation() {
        return recommendedBy;
    }

    public Set<Vehicle> getVehicles() {
        return new HashSet<>(vehicles);
    }

    public Set<PaymentMean> getPaymentMeans() {
        return new HashSet<>(paymentMeans);
    }

    public Set<Recommendation> getRecommendations() {
        return new HashSet<>(recommendations);
    }

    public List<WorkOrder> getWorkOrdersAvailableForVoucher20() {
        return vehicles.stream().flatMap(vehicle -> vehicle.getWorkOrders().stream())
            .filter(workOrder -> workOrder.canBeUsedForVoucher()).toList();
    }

    public List<Recommendation> getRecommendationsAvailableForVoucher25() {
        return recommendations.stream()
            .filter(recommendation -> recommendation.getRecommended().hasAWorkOrderdAndAnInvoicePaid())
            .filter(recommendation -> !recommendation.isUsedForVoucher()).toList();
    }

    public void markThreeRecommendationsAsUsedForVoucher25() {
        List<Recommendation> available = getRecommendationsAvailableForVoucher25();
        StateChecks.isTrue(available.size() >= 3, "Not enough recommendations available to mark as used");
        available.stream().limit(3).forEach(recommendation -> recommendation.markAsUsedForVoucher());
    }

    public void markThreeWorkOrdersAsUsedForVoucher20() {
        List<WorkOrder> available = getWorkOrdersAvailableForVoucher20();
        StateChecks.isTrue(available.size() >= 3, "Not enough work orders available to mark as used");
        available.stream().limit(3).forEach(workOrder -> workOrder.markAsUsedForVoucher());
    }

    private boolean hasRecommendedThreeClients() {
        return recommendations.size() >= 3;
    }

    private boolean hasAWorkOrderdAndAnInvoicePaid() {
        return vehicles.stream().flatMap(vehicle -> vehicle.getWorkOrders().stream())
            .anyMatch(w -> w.isInvoiced() && w.getInvoice().getState().equals(Invoice.InvoiceState.PAID));
    }

    private boolean isValidRecommendation() {
        Set<Recommendation> recommendations = getRecommendations();
        int valid = 0;
        for (Recommendation recommendation : recommendations) {
            if (recommendation.getRecommended().hasAWorkOrderdAndAnInvoicePaid()
                && !recommendation.isUsedForVoucher()) {
                valid++;
            }
        }

        return valid >= 3;
    }

    public boolean isEligibleForVoucher25() {
        return hasRecommendedThreeClients() && hasAWorkOrderdAndAnInvoicePaid() && isValidRecommendation();
    }

    // Association helpers
    /* package */
    Set<Vehicle> _getVehicles() {
        return vehicles;
    }

    /* package */
    Set<PaymentMean> _getPaymentMeans() {
        return paymentMeans;
    }

    Set<Recommendation> _getRecommendations() {
        return recommendations;
    }

    void _setRecommendation(Recommendation recommendation) {
        this.recommendedBy = recommendation;
    }

}