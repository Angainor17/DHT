package cx.ring.account;

import android.graphics.Bitmap;

import cx.ring.mvp.AccountCreationModel;
import cx.ring.utils.BitmapUtils;
import ezvcard.VCard;
import ezvcard.property.FormattedName;
import ezvcard.property.Photo;
import ezvcard.property.RawProperty;
import ezvcard.property.Uid;
import io.reactivex.Single;

public class AccountCreationModelImpl extends AccountCreationModel {

    @Override
    public Single<VCard> toVCard() {
        return Single
                .fromCallable(() -> {
                    VCard vcard = new VCard();
                    vcard.setFormattedName(new FormattedName(getFullName()));
                    vcard.setUid(new Uid(getUsername()));
                    Bitmap bmp = getPhoto();
                    if (bmp != null) {
                        vcard.removeProperties(Photo.class);
                        vcard.addPhoto(BitmapUtils.bitmapToPhoto(bmp));
                    }
                    vcard.removeProperties(RawProperty.class);
                    return vcard;
                });
    }

    @Override
    public Bitmap getPhoto() {
        return (Bitmap) super.getPhoto();
    }

    public void setPhoto(Bitmap photo) {
        super.setPhoto(photo);
    }
}
