using System;
using System.ComponentModel;

namespace AcafelaUserAdmin
{
    public interface IUserEnableHandler
    {
        int HandleUserEnable(String email, String phone, bool enable);
    }

    public class UserInfo : INotifyPropertyChanged
    {
        public event PropertyChangedEventHandler PropertyChanged;

        private IUserEnableHandler mUserEnableHandler;

        private String mEmail;
        private String mPhone;
        private String mIpAddress;
        private bool mEnabled;

        public String Email
        {
            get { return mEmail; }
            set { mEmail = value; NotifyPropertyChanged("Email"); }
        }

        public String Phone
        {
            get { return mPhone; }
            set { mPhone = value; NotifyPropertyChanged("Phone"); }
        }

        public String IpAddress
        {
            get { return mIpAddress; }
            set { mIpAddress = value; NotifyPropertyChanged("IpAddress"); }
        }

        public bool Enabled
        {
            get { return mEnabled; }
            set
            {
                int err = mUserEnableHandler?.HandleUserEnable(
                                                mEmail, mPhone, value) ?? -1;
                if (err == 0)
                {
                    mEnabled = value;
                    NotifyPropertyChanged("Enabled");
                }
            }
        }

        public UserInfo(
                    String email,
                    String phone,
                    String ipAddress,
                    bool enabled)
        {
            mEmail = email;
            mPhone = phone;
            mIpAddress = ipAddress;
            mEnabled = enabled;
        }

        public void SetUserEnableHandler(IUserEnableHandler handler)
            => mUserEnableHandler = handler;


        private void NotifyPropertyChanged(string propertyName)
        {
            if (string.IsNullOrEmpty(propertyName) || PropertyChanged == null)
                return;
            PropertyChanged?.Invoke(
                                this,
                                new PropertyChangedEventArgs(propertyName));
        }
    }
}
