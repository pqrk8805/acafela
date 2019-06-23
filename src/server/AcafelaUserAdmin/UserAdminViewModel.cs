using System;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Windows;

namespace AcafelaUserAdmin
{
    public class UserAdminViewModel : IUserEnableHandler
    {
        private UserAdminModel mModel;

        private ObservableCollection<UserInfo> mUserInfos;
        public ObservableCollection<UserInfo> UserInfos
        {
            get { return mUserInfos; }
            set { mUserInfos = value; }
        }

        public UserAdminViewModel()
        {
            Console.WriteLine("UserAdminViewModel()");

            mModel = (Application.Current as App)?.UserAdminModel;

            UserInfos = new ObservableCollection<UserInfo>();
            var infos = mModel.GetUserInfo();
            foreach (var info in infos)
            {
                Console.WriteLine($"{info.Email} {info.PhoneNumber} {info.IpAddress} {info.Enabled}");
                var item = new UserInfo(
                                    info.Email,
                                    info.PhoneNumber,
                                    info.IpAddress,
                                    info.Enabled);
                item.SetUserEnableHandler(this);
                UserInfos.Add(item);
            }
        }

        public int HandleUserEnable(String email, bool enable)
        {
            Console.WriteLine($"UserAdminViewModel.OnPropertyChanged() {email} {enable}");
            return enable
                    ? mModel.EnableUser(email)
                    : mModel.DisableUser(email);
        }
    }
}
