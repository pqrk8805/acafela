using System;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Windows;

namespace AcafelaUserAdmin
{
    public class UserAdminViewModel : INotifyPropertyChanged
    {
        private UserAdminModel mModel;

        private ObservableCollection<UserInfo> mUserInfos;
        public ObservableCollection<UserInfo> UserInfos
        {
            get { return mUserInfos; }
            set { mUserInfos = value; } // OnPropertyChanged("UserInfos"); }
        }

        public event PropertyChangedEventHandler PropertyChanged;

        public UserAdminViewModel()
        {
            Console.WriteLine("UserAdminViewModel()");

            mModel = (Application.Current as App)?.UserAdminModel;

            var userInfos = new ObservableCollection<UserInfo>();
            var infos = mModel.GetUserInfo();
            foreach (var info in infos)
            {
                Console.WriteLine($"{info.Email} {info.PhoneNumber} {info.Enabled}");
                userInfos.Add(new UserInfo {
                                        Selected = false,
                                        Email = info.Email,
                                        Phone = info.PhoneNumber,
                                        Enabled = info.Enabled,
                                    });
            }
            UserInfos = userInfos;
        }

        private void OnPropertyChanged(string propertyName)
        {
            if (string.IsNullOrEmpty(propertyName) || PropertyChanged == null)
                return;
            PropertyChanged?.Invoke(
                                this,
                                new PropertyChangedEventArgs(propertyName));
        }
    }
}
