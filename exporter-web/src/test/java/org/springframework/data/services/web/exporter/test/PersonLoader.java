package org.springframework.data.services.web.exporter.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class PersonLoader implements InitializingBean {

  private PersonRepository personRepository;
  private ProfileRepository profileRepository;
  private AddressRepository addressRepository;

  public PersonRepository getPersonRepository() {
    return personRepository;
  }

  public void setPersonRepository(PersonRepository personRepository) {
    this.personRepository = personRepository;
  }

  public ProfileRepository getProfileRepository() {
    return profileRepository;
  }

  public void setProfileRepository(ProfileRepository profileRepository) {
    this.profileRepository = profileRepository;
  }

  public AddressRepository getAddressRepository() {
    return addressRepository;
  }

  public void setAddressRepository(AddressRepository addressRepository) {
    this.addressRepository = addressRepository;
  }

  @Override public void afterPropertiesSet() throws Exception {
    Address pers1addr = addressRepository.save(new Address(new String[]{"1234 W. 1st St."}, "Univille", "ST", "12345"));

    Map<String, Profile> pers1profiles = new HashMap<String, Profile>();
    Profile twitter = profileRepository.save(new Profile("twitter", "#!/johndoe"));
    Profile fb = profileRepository.save(new Profile("facebook", "/johndoe"));
    pers1profiles.put("twitter", twitter);
    pers1profiles.put("facebook", fb);

    personRepository.save(new Person(1L, "John Doe", Arrays.asList(pers1addr), pers1profiles));

    Address pers2addr = addressRepository.save(new Address(new String[]{"1234 E. 2nd St."}, "Univille", "ST", "12345"));

    Map<String, Profile> pers2profiles = new HashMap<String, Profile>();
    Profile twitter2 = profileRepository.save(new Profile("twitter", "#!/janedoe"));
    Profile fb2 = profileRepository.save(new Profile("facebook", "/janedoe"));
    pers2profiles.put("facebook", fb2);

    personRepository.save(new Person(2L, "Jane Doe", Arrays.asList(pers2addr), pers2profiles));
  }

}
