import { onBeforeRouteLeave } from 'vue-router';

import { useI18n } from '@/hooks/useI18n';
import useModal from '@/hooks/useModal';

const isSave = ref(false);

// 离开页面确认提示
export default function useLeaveUnSaveTip() {
  const { openModal } = useModal();
  const { t } = useI18n();

  const setState = (flag: boolean) => {
    isSave.value = flag;
  };
  onBeforeRouteLeave((to, from, next) => {
    if (to.path === from.path) {
      return;
    }

    if (!isSave.value) {
      openModal({
        type: 'error',
        title: t('common.unSaveLeaveTitle'),
        content: t('common.unSaveLeaveContent'),
        okText: t('common.leave'),
        cancelText: t('common.cancel'),
        okButtonProps: {
          status: 'normal',
        },
        onBeforeOk: async () => {
          next();
        },
        hideCancel: false,
      });
    } else {
      next();
    }
  });
  return {
    setState,
  };
}
