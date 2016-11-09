LOCAL_DIR := $(call my-dir)
VCMI_DIR := $(LOCAL_DIR)/vcmi-app

FILTER_OUT = $(foreach v,$2,$(if $(findstring $1,$v),,$v))

define vcmiwalk
  $(wildcard $(1)) $(foreach e, $(wildcard $(1)/*), $(call vcmiwalk, $(e)))
endef

include $(LOCAL_DIR)/vcmi-lib.mk