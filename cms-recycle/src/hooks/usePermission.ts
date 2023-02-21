import useLoginStore from '@/store/login/login'

function usePermission(permissionID: string) {
  const loginStore = useLoginStore()
  const { permissions } = loginStore

  return !!permissions.find((item: string | string[]) =>
    item.includes(permissionID)
  )
}

export default usePermission
