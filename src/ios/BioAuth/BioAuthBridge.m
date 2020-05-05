//
//  BioAuthBridge.m
//  bioAuth
//
//  Created by Tomer Kobrinsky on 26/09/2019.
//  Copyright © 2019 Cherrypick Consulting LTD.. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTUtils.h>

@interface RCT_EXTERN_MODULE(BioAuth, NSObject)

RCT_EXTERN_METHOD(authenticate : (NSString)authenticationDescription
                  onSuccess:(RCTResponseSenderBlock) onSuccess
                  onFailure: (RCTResponseSenderBlock) onFailure
                  )


@end
