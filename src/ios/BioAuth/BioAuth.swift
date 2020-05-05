//
//  BioAuth.swift
//  bioAuth
//
//  Created by Tomer Kobrinsky on 26/09/2019.
//  Copyright © 2019 Cherrypick Consulting LTD. All rights reserved.
//

import Foundation
import LocalAuthentication

@objc (BioAuth)
class BioAuth: NSObject {
  static var isSimulator: Bool {
     return TARGET_OS_SIMULATOR != 0
  }
  enum errors: String {
    case authenticationFailed = "authenticationFailed"
    case noAuthenticationOnDevice = "noAuthenticationOnDevice"
  }
  override init() {
    super.init()
  }
  
  @objc static func requiresMainQueueSetup() -> Bool {
    return false
  }
  
  @objc func authenticate(_ authenticationDescription: NSString, onSuccess: @escaping RCTResponseSenderBlock, onFailure: @escaping RCTResponseSenderBlock) {
    let authenticator = LAContext()
    
    // since simulator don't really replicate device authentication well(no option to add authentication but prompts a passcode ui and acts as if any passcode will pass except empty passcode). it make sense that we act as if authentication succeeded
    if (BioAuth.isSimulator) {
      onSuccess(["success"])
    } // User authentication with either biometry, Apple watch or the device passcode.
    else if (authenticator.canEvaluatePolicy(LAPolicy.deviceOwnerAuthentication, error: nil)) {
      authenticator.evaluatePolicy(LAPolicy.deviceOwnerAuthentication, localizedReason: authenticationDescription as String) { (didSucceed, error) in
        if (didSucceed) {
          onSuccess(["success"])
        } else {
          onFailure([errors.authenticationFailed.rawValue])
        }
      }
    } else {
      onFailure([errors.noAuthenticationOnDevice.rawValue])
    }
  }
}
