using System;
using System.Collections;
using System.Collections.ObjectModel;
using System.Windows;

namespace AcafelaUserAdmin
{
    public class UserAdminViewModel : IUserEnableHandler
    {
        private UserAdminModel mModel;
        public ObservableCollection<UserInfo> UserInfos { get; set; }

        public UserAdminViewModel()
        {
            Console.WriteLine("UserAdminViewModel()");

            mModel = (Application.Current as App)?.UserAdminModel;
            UserInfos = new ObservableCollection<UserInfo>();
            RefreshEntry();
        }

        public int HandleUserEnable(String email, bool enable)
        {
            Console.WriteLine($"UserAdminViewModel.OnPropertyChanged() {email} {enable}");
            return enable
                    ? mModel.EnableUser(email)
                    : mModel.DisableUser(email);
        }

        public void RefreshEntry()
        {
            Console.WriteLine("RefreshEntry()");

            UserInfos.Clear();
            var infos = mModel.GetUserInfo();
            foreach (var info in infos)
            {
                Console.WriteLine($"    {info.Email} {info.PhoneNumber} {info.IpAddress} {info.Enabled}");
                var item = new UserInfo(
                                    info.Email,
                                    info.PhoneNumber,
                                    info.IpAddress,
                                    info.Enabled);
                item.SetUserEnableHandler(this);
                UserInfos.Add(item);
            }
        }

        public void DeleteUser(IList userInfos)
        {
            Console.WriteLine("DeleteUser()");

            foreach (UserInfo info in userInfos)
            {
                Console.WriteLine($"  {info.Email}");
                mModel.DeleteUser(info.Email);
            }
            RefreshEntry();
        }
    }
}
