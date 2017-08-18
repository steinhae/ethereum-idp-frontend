package de.tum.repairchain.ipfs;

import javax.inject.Singleton;

import dagger.Component;
import de.tum.repairchain.MainActivity;
import de.tum.repairchain.UploadImage;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {
    void inject(MainActivity mainActivity);

    void inject(HashTextAndBarcodeActivity addIPFSContent);

    void inject(UploadImage uploadImage);

}
