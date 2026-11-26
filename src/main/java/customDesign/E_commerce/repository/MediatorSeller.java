package customDesign.E_commerce.repository;

import customDesign.E_commerce.dto.PagedApiResponse;
import customDesign.E_commerce.dto.mediator.*;

public interface MediatorSeller {
    SellerInviteResponse inviteSeller(SellerInviteRequest request);

    PagedApiResponse<SellerSummaryResponse> listSellers(String status, int page, int size);

    SellerSummaryResponse getSeller(Long id);

    SellerStatusResponse approveSeller(Long id, SellerActionRequest req);

    SellerStatusResponse rejectSeller(Long id, SellerActionRequest req);

    SellerStatusResponse suspendSeller(Long id, SellerActionRequest req);

}