package de.tum.repairchain.ipfs;

import javax.inject.Singleton;

import dagger.Component;
import de.tum.repairchain.UploadImage;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {

    void inject(HashTextAndBarcodeActivity addIPFSContent);

}
