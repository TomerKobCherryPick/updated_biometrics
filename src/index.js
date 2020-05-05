const authenticator = NativeModules.BioAuth
import {NativeModules, Platform} from 'react-native'

const errors = {
  authenticationFailed: "authenticationFailed",
  noAuthenticationOnDevice: "noAuthenticationOnDevice"
}

const authenticationText = {
  getTitle: () =>  "Authentication",
  getDescription: () => "Please authenticate to continue",
}

const authenticate = async (onSuccess, onFailure, title, description) => {
    const authenticationTitle = title ?? authenticationText.getTitle()
    const authenticationDescription = description ?? authenticationText.getDescription()
    if (Platform.OS === 'android') {
      await authenticator.authenticate(authenticationTitle, authenticationDescription, onSuccess, onFailure)
    } else {
      // in ios there is no option to change the title of authentication alert
      await authenticator.authenticate(authenticationDescription, onSuccess, onFailure)
    }
}

export default { authenticate, errors }
