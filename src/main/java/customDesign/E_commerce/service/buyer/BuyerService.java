package customDesign.E_commerce.service.buyer;

import customDesign.E_commerce.dto.AddressRequest;
import customDesign.E_commerce.entity.Address;
import customDesign.E_commerce.repository.AddressRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BuyerService {

    private final AddressRepository addressRepository;

    public BuyerService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public List<Address> getAddresses(String email) {
        return addressRepository.findByUserEmail(email);
    }

    public Address addAddress(String email, AddressRequest req) {
        Address address = new Address();
        address.setUserEmail(email);
        address.setFullName(req.getFullName());
        address.setPhone(req.getPhone());
        address.setLine1(req.getLine1());
        address.setLine2(req.getLine2());
        address.setCity(req.getCity());
        address.setState(req.getState());
        address.setPincode(req.getPincode());
        address.setDefault(req.isDefault());
        return addressRepository.save(address);
    }
}