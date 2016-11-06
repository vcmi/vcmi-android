LOCAL_DIR := $(call my-dir)
VCMI_DIR := $(LOCAL_DIR)/vcmi-app

define vcmiwalk
  $(wildcard $(1)) $(foreach e, $(wildcard $(1)/*), $(call vcmiwalk, $(e)))
endef

include $(CLEAR_VARS)
include $(LOCAL_DIR)/fuzzylite.mk
include $(CLEAR_VARS)
include $(LOCAL_DIR)/minizip.mk
include $(CLEAR_VARS)
include $(LOCAL_DIR)/ai-empty.mk